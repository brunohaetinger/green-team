package processor

import (
    "context"
    "encoding/json"
    "os"
    "runtime"
    "strings"
    "time"

    redis "github.com/redis/go-redis/v9"

    "github.com/thiagonasc/poll/internal/models"
    "github.com/thiagonasc/poll/internal/store"
)

type Processor struct {
    store      store.Store
    queue      chan models.VoteRequest
    workerDone []chan struct{}

    redisEnabled bool
    rdb          *redis.Client
    redisQueue   string
    ctx          context.Context
    cancel       context.CancelFunc
}

func New(s store.Store, buffer int, workers int) *Processor {
    if workers <= 0 {
        workers = runtime.NumCPU() * 128
    }
    if workers < 512 {
        workers = 512
    }
    if workers > 8192 {
        workers = 8192
    }
    p := &Processor{store: s}

    redisURL := strings.TrimSpace(os.Getenv("REDIS_URL"))
    queueName := strings.TrimSpace(os.Getenv("REDIS_QUEUE_NAME"))
    if queueName == "" {
        queueName = "votes"
    }
    if redisURL != "" {
        opt, err := redis.ParseURL(redisURL)
        if err == nil {
            rdb := redis.NewClient(opt)
            ctx, cancel := context.WithCancel(context.Background())
            if _, pingErr := rdb.Ping(ctx).Result(); pingErr == nil {
                p.redisEnabled = true
                p.rdb = rdb
                p.redisQueue = queueName
                p.ctx = ctx
                p.cancel = cancel
            } else {
                cancel()
                _ = rdb.Close()
            }
        }
    }

    if p.redisEnabled {
        p.workerDone = make([]chan struct{}, workers)
        for i := 0; i < workers; i++ {
            done := make(chan struct{})
            p.workerDone[i] = done
            go func() {
                defer close(done)
                for {
                    vals, err := p.rdb.BLPop(p.ctx, 5*time.Second, p.redisQueue).Result()
                    if err != nil {
                        if err == context.Canceled || err == context.DeadlineExceeded {
                            if p.ctx.Err() != nil {
                                return
                            }
                            continue
                        }
                        if p.ctx.Err() != nil {
                            return
                        }
                        continue
                    }
                    if len(vals) == 2 {
                        var v models.VoteRequest
                        if json.Unmarshal([]byte(vals[1]), &v) == nil {
                            _ = p.store.ApplyVote(v)
                        }
                    }
                }
            }()
        }
        return p
    }

    p.queue = make(chan models.VoteRequest, buffer)
    p.workerDone = make([]chan struct{}, workers)
    for i := 0; i < workers; i++ {
        done := make(chan struct{})
        p.workerDone[i] = done
        go func() {
            for v := range p.queue {
                _ = p.store.ApplyVote(v)
            }
            close(done)
        }()
    }
    return p
}

func (p *Processor) Enqueue(v models.VoteRequest) bool {
    if p.redisEnabled {
        b, err := json.Marshal(v)
        if err != nil {
            return false
        }
        if err := p.rdb.RPush(p.ctx, p.redisQueue, b).Err(); err != nil {
            return false
        }
        return true
    }
    select {
    case p.queue <- v:
        return true
    default:
        return false
    }
}

func (p *Processor) Close() {
    if p.redisEnabled {
        if p.cancel != nil {
            p.cancel()
        }
        for _, d := range p.workerDone {
            <-d
        }
        if p.rdb != nil {
            _ = p.rdb.Close()
        }
        return
    }
    close(p.queue)
    for _, d := range p.workerDone {
        <-d
    }
}
