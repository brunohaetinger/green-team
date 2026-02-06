package api

import (
	"encoding/json"
	"log"
	"net/http"
	"os"
	"sort"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/thiagonasc/poll/internal/models"
	"github.com/thiagonasc/poll/internal/processor"
	"github.com/thiagonasc/poll/internal/seed"
	"github.com/thiagonasc/poll/internal/store"
)

type OptionItemDTO struct {
	ID    uint32 `json:"id"`
	Label string `json:"label"`
	Votes int    `json:"votes"`
}

type PollResponse struct {
	ID       uint32          `json:"id"`
	Question string          `json:"question"`
	IsOpen   bool            `json:"is_open"`
	Options  []OptionItemDTO `json:"options"`
	Voters   []string        `json:"voters"`
}

type Server struct {
	store  store.Store
	votes  *processor.Processor
	closer func()
	router *gin.Engine
}

func NewServer() *Server {
	var st store.Store
	var closer func()
	dsn := strings.TrimSpace(os.Getenv("DB_URL"))
	backend := strings.ToLower(strings.TrimSpace(os.Getenv("STORE_BACKEND")))

	switch backend {
	case "postgres", "pg", "postgresql":
		if dsn != "" {
			if pg, c, err := store.NewPostgres(dsn); err == nil {
				st = pg
				closer = c
			} else {
				log.Printf("failed to init postgres store: %v, falling back to memory", err)
			}
		} else {
			log.Printf("STORE_BACKEND=postgres but DB_URL is empty; falling back to memory store")
		}
	case "memory", "mem", "inmemory", "in-memory":
	default:
		if dsn != "" {
			if pg, c, err := store.NewPostgres(dsn); err == nil {
				st = pg
				closer = c
			} else {
				log.Printf("failed to init postgres store: %v, falling back to memory", err)
			}
		}
	}
	if st == nil {
		ms := store.New()
		seed.SeedDemo(ms)
		st = ms
		closer = func() {}
	}

	bufSize := 1_000_000
	if v := strings.TrimSpace(os.Getenv("VOTE_QUEUE_SIZE")); v != "" {
		if n, err := strconv.Atoi(v); err == nil && n > 0 {
			bufSize = n
		} else if v != "" {
			log.Printf("invalid VOTE_QUEUE_SIZE=%q, using default %d", v, bufSize)
		}
	}
	workers := 0
	if v := strings.TrimSpace(os.Getenv("VOTE_WORKERS")); v != "" {
		if n, err := strconv.Atoi(v); err == nil && n > 0 {
			workers = n
		} else {
			log.Printf("invalid VOTE_WORKERS=%q, using auto", v)
		}
	}
	vp := processor.New(st, bufSize, workers)

	// Gin setup: default to release mode unless explicitly overridden
	if strings.TrimSpace(os.Getenv("GIN_MODE")) == "" {
		gin.SetMode(gin.ReleaseMode)
	}
	r := gin.New()
	// minimal middleware for performance
	r.Use(gin.Recovery())

	srv := &Server{store: st, votes: vp, closer: closer, router: r}
	srv.Routes()
	return srv
}

func (s *Server) Routes() {
	// Swagger endpoints
	registerSwagger(s.router)

	// Map existing http.Handler funcs through Gin wrappers for minimal changes
	s.router.GET("/health", func(c *gin.Context) { c.Status(http.StatusOK) })
	s.router.POST("/vote", func(c *gin.Context) { s.handleVote(c.Writer, c.Request) })
	s.router.POST("/polls", func(c *gin.Context) { s.handlePolls(c.Writer, c.Request) })
	s.router.POST("/options", func(c *gin.Context) { s.handleOptions(c.Writer, c.Request) })
}

func (s *Server) Close() {
	s.votes.Close()
	if s.closer != nil {
		s.closer()
	}
}

// Handler exposes the Gin router as an http.Handler for the HTTP server.
func (s *Server) Handler() http.Handler {
	return s.router
}

func (s *Server) handleVote(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
		return
	}
	var req models.VoteRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "invalid JSON", http.StatusBadRequest)
		return
	}
	req.VoterID = strings.TrimSpace(req.VoterID)
	if req.PollID == 0 || req.OptionID == 0 || req.VoterID == "" {
		http.Error(w, "poll_id, option_id, voter_id are required", http.StatusBadRequest)
		return
	}

	if ok := s.votes.Enqueue(req); !ok {
		http.Error(w, "server is busy, please retry", http.StatusServiceUnavailable)
		return
	}
	w.WriteHeader(http.StatusOK)
}

func (s *Server) handleGetOption(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
		return
	}
	idStr := strings.TrimSpace(r.URL.Query().Get("id"))
	if idStr == "" {
		http.Error(w, "id is required", http.StatusBadRequest)
		return
	}
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		http.Error(w, "invalid id", http.StatusBadRequest)
		return
	}
	opt, ok := s.store.GetOption(uint32(id))
	if !ok {
		http.Error(w, "option not found", http.StatusNotFound)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	_ = json.NewEncoder(w).Encode(OptionItemDTO{ID: opt.ID, Label: opt.Label, Votes: opt.Votes})
}

type createPollReq struct {
	ID       uint32 `json:"id"`
	Question string `json:"question"`
	IsOpen   *bool  `json:"is_open"`
}

func (s *Server) handlePolls(w http.ResponseWriter, r *http.Request) {
	switch r.Method {
	case http.MethodGet:
		idStr := strings.TrimSpace(r.URL.Query().Get("id"))
		w.Header().Set("Content-Type", "application/json")
		if idStr == "" {
			snaps := s.store.ListPollSnapshots()
			out := make([]PollResponse, 0, len(snaps))
			for _, snap := range snaps {
				pr := PollResponse{ID: snap.ID, Question: snap.Question, IsOpen: snap.IsOpen}
				for _, o := range snap.Options {
					pr.Options = append(pr.Options, OptionItemDTO{ID: o.ID, Label: o.Label, Votes: o.Votes})
				}
				pr.Voters = snap.Voters
				out = append(out, pr)
			}
			_ = json.NewEncoder(w).Encode(out)
			return
		}
		id, err := strconv.ParseUint(idStr, 10, 32)
		if err != nil {
			http.Error(w, "invalid id", http.StatusBadRequest)
			return
		}
		snap, ok := s.store.GetPollSnapshot(uint32(id))
		if !ok {
			http.Error(w, "not found", http.StatusNotFound)
			return
		}
		resp := PollResponse{ID: snap.ID, Question: snap.Question, IsOpen: snap.IsOpen}
		for _, o := range snap.Options {
			resp.Options = append(resp.Options, OptionItemDTO{ID: o.ID, Label: o.Label, Votes: o.Votes})
		}
		resp.Voters = snap.Voters
		_ = json.NewEncoder(w).Encode(resp)
	case http.MethodPost:
		var req createPollReq
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			http.Error(w, "invalid JSON", http.StatusBadRequest)
			return
		}
		q := strings.TrimSpace(req.Question)
		isOpen := true
		if req.IsOpen != nil {
			isOpen = *req.IsOpen
		}
		if req.ID == 0 || q == "" {
			http.Error(w, "id and question are required", http.StatusBadRequest)
			return
		}
		if err := s.store.CreatePoll(req.ID, q, isOpen); err != nil {
			status := http.StatusBadRequest
			if strings.Contains(err.Error(), "already exists") {
				status = http.StatusConflict
			} else if strings.Contains(err.Error(), "not found") {
				status = http.StatusNotFound
			}
			http.Error(w, err.Error(), status)
			return
		}
		w.WriteHeader(http.StatusOK)
		return
	case http.MethodPut:
		var req createPollReq
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			http.Error(w, "invalid JSON", http.StatusBadRequest)
			return
		}
		if req.IsOpen == nil {
			b := true
			req.IsOpen = &b
		}
		q := strings.TrimSpace(req.Question)
		if req.ID == 0 || q == "" {
			http.Error(w, "id and question are required", http.StatusBadRequest)
			return
		}
		if err := s.store.UpdatePoll(req.ID, q, *req.IsOpen); err != nil {
			status := http.StatusBadRequest
			if strings.Contains(err.Error(), "not found") {
				status = http.StatusNotFound
			}
			http.Error(w, err.Error(), status)
			return
		}
		w.WriteHeader(http.StatusNoContent)
		return
	case http.MethodDelete:
		idStr := strings.TrimSpace(r.URL.Query().Get("id"))
		if idStr == "" {
			http.Error(w, "id is required", http.StatusBadRequest)
			return
		}
		id, err := strconv.ParseUint(idStr, 10, 32)
		if err != nil {
			http.Error(w, "invalid id", http.StatusBadRequest)
			return
		}
		if err := s.store.DeletePoll(uint32(id)); err != nil {
			status := http.StatusBadRequest
			if strings.Contains(err.Error(), "not found") {
				status = http.StatusNotFound
			}
			http.Error(w, err.Error(), status)
			return
		}
		w.WriteHeader(http.StatusNoContent)
		return
	default:
		http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
		return
	}
}

type optionReq struct {
	ID     uint32 `json:"id"`
	PollID uint32 `json:"poll_id"`
	Label  string `json:"label"`
}

func (s *Server) handleOptions(w http.ResponseWriter, r *http.Request) {
	switch r.Method {
	case http.MethodGet:
		idStr := strings.TrimSpace(r.URL.Query().Get("id"))
		w.Header().Set("Content-Type", "application/json")
		if idStr != "" {
			id, err := strconv.ParseUint(idStr, 10, 32)
			if err != nil {
				http.Error(w, "invalid id", http.StatusBadRequest)
				return
			}
			if opt, ok := s.store.GetOption(uint32(id)); ok {
				dto := OptionItemDTO{ID: opt.ID, Label: opt.Label, Votes: opt.Votes}
				_ = json.NewEncoder(w).Encode(dto)
				return
			}
			http.Error(w, "not found", http.StatusNotFound)
			return
		}
		pollIDStr := strings.TrimSpace(r.URL.Query().Get("poll_id"))
		var pollID uint32
		if pollIDStr != "" {
			id, err := strconv.ParseUint(pollIDStr, 10, 32)
			if err != nil {
				http.Error(w, "invalid poll_id", http.StatusBadRequest)
				return
			}
			pollID = uint32(id)
		}
		items := s.store.ListOptions(pollID)
		out := make([]OptionItemDTO, 0, len(items))
		for _, o := range items {
			out = append(out, OptionItemDTO{ID: o.ID, Label: o.Label, Votes: o.Votes})
		}
		_ = json.NewEncoder(w).Encode(out)
	case http.MethodPost:
		var req optionReq
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			http.Error(w, "invalid JSON", http.StatusBadRequest)
			return
		}
		if req.PollID == 0 || req.ID == 0 || strings.TrimSpace(req.Label) == "" {
			http.Error(w, "poll_id, id, label are required", http.StatusBadRequest)
			return
		}
		if err := s.store.AddOption(req.PollID, req.ID, strings.TrimSpace(req.Label)); err != nil {
			status := http.StatusBadRequest
			if strings.Contains(err.Error(), "not found") {
				status = http.StatusNotFound
			} else if strings.Contains(err.Error(), "already exists") {
				status = http.StatusConflict
			}
			http.Error(w, err.Error(), status)
			return
		}
		w.WriteHeader(http.StatusCreated)
	case http.MethodPut:
		var req optionReq
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			http.Error(w, "invalid JSON", http.StatusBadRequest)
			return
		}
		if req.ID == 0 || strings.TrimSpace(req.Label) == "" {
			http.Error(w, "id and label are required", http.StatusBadRequest)
			return
		}
		if err := s.store.UpdateOption(req.ID, strings.TrimSpace(req.Label)); err != nil {
			status := http.StatusBadRequest
			if strings.Contains(err.Error(), "not found") {
				status = http.StatusNotFound
			}
			http.Error(w, err.Error(), status)
			return
		}
		w.WriteHeader(http.StatusNoContent)
	case http.MethodDelete:
		idStr := strings.TrimSpace(r.URL.Query().Get("id"))
		if idStr == "" {
			http.Error(w, "id is required", http.StatusBadRequest)
			return
		}
		id, err := strconv.ParseUint(idStr, 10, 32)
		if err != nil {
			http.Error(w, "invalid id", http.StatusBadRequest)
			return
		}
		if err := s.store.DeleteOption(uint32(id)); err != nil {
			status := http.StatusBadRequest
			if strings.Contains(err.Error(), "not found") {
				status = http.StatusNotFound
			}
			http.Error(w, err.Error(), status)
			return
		}
		w.WriteHeader(http.StatusNoContent)
	default:
		http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
	}
}

type voterReq struct {
	PollID  uint32 `json:"poll_id"`
	VoterID string `json:"voter_id"`
}

func (s *Server) handleVoters(w http.ResponseWriter, r *http.Request) {
	switch r.Method {
	case http.MethodGet:
		pidStr := strings.TrimSpace(r.URL.Query().Get("poll_id"))
		if pidStr == "" {
			http.Error(w, "poll_id is required", http.StatusBadRequest)
			return
		}
		pid, err := strconv.ParseUint(pidStr, 10, 32)
		if err != nil {
			http.Error(w, "invalid poll_id", http.StatusBadRequest)
			return
		}
		snap, ok := s.store.GetPollSnapshot(uint32(pid))
		if !ok {
			http.Error(w, "poll not found", http.StatusNotFound)
			return
		}
		resp := struct {
			PollID uint32   `json:"poll_id"`
			Voters []string `json:"voters"`
		}{PollID: uint32(pid), Voters: snap.Voters}
		w.Header().Set("Content-Type", "application/json")
		_ = json.NewEncoder(w).Encode(resp)
	case http.MethodPost:
		var req voterReq
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			http.Error(w, "invalid JSON", http.StatusBadRequest)
			return
		}
		vid := strings.TrimSpace(req.VoterID)
		if req.PollID == 0 || vid == "" {
			http.Error(w, "poll_id and voter_id are required", http.StatusBadRequest)
			return
		}
		if err := s.store.AddVoter(req.PollID, vid); err != nil {
			status := http.StatusBadRequest
			if strings.Contains(err.Error(), "not found") {
				status = http.StatusNotFound
			} else if strings.Contains(err.Error(), "already exists") {
				status = http.StatusConflict
			}
			http.Error(w, err.Error(), status)
			return
		}
		w.WriteHeader(http.StatusCreated)
	case http.MethodDelete:
		pidStr := strings.TrimSpace(r.URL.Query().Get("poll_id"))
		vid := strings.TrimSpace(r.URL.Query().Get("voter_id"))
		if pidStr == "" || vid == "" {
			http.Error(w, "poll_id and voter_id are required", http.StatusBadRequest)
			return
		}
		pid, err := strconv.ParseUint(pidStr, 10, 32)
		if err != nil {
			http.Error(w, "invalid poll_id", http.StatusBadRequest)
			return
		}
		if err := s.store.DeleteVoter(uint32(pid), vid); err != nil {
			status := http.StatusBadRequest
			if strings.Contains(err.Error(), "not found") {
				status = http.StatusNotFound
			}
			http.Error(w, err.Error(), status)
			return
		}
		w.WriteHeader(http.StatusNoContent)
	default:
		http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
	}
}

func (s *Server) handleGetPoll(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
		return
	}
	idStr := strings.TrimSpace(r.URL.Query().Get("id"))
	if idStr == "" {
		http.Error(w, "id is required", http.StatusBadRequest)
		return
	}
	id, err := strconv.ParseUint(idStr, 10, 32)
	if err != nil {
		http.Error(w, "invalid id", http.StatusBadRequest)
		return
	}
	snap, ok := s.store.GetPollSnapshot(uint32(id))
	if !ok {
		http.Error(w, "poll not found", http.StatusNotFound)
		return
	}
	resp := PollResponse{ID: snap.ID, Question: snap.Question, IsOpen: snap.IsOpen}
	opts := make([]OptionItemDTO, 0, len(snap.Options))
	for _, o := range snap.Options {
		opts = append(opts, OptionItemDTO{ID: o.ID, Label: o.Label, Votes: o.Votes})
	}
	sort.Slice(opts, func(i, j int) bool { return opts[i].Label < opts[j].Label })
	resp.Options = opts
	resp.Voters = snap.Voters
	w.Header().Set("Content-Type", "application/json")
	_ = json.NewEncoder(w).Encode(resp)
}
