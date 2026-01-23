package store

import (
    "database/sql"
    "errors"
    "fmt"
    "sort"
    "strings"

    _ "github.com/lib/pq"
    "github.com/thiagonasc/poll/internal/models"
)

type PostgresStore struct {
    db *sql.DB
}

func NewPostgres(dsn string) (Store, func(), error) {
    db, err := sql.Open("postgres", dsn)
    if err != nil {
        return nil, nil, err
    }
    if err := db.Ping(); err != nil {
        _ = db.Close()
        return nil, nil, err
    }
    p := &PostgresStore{db: db}
    if err := p.migrate(); err != nil {
        _ = db.Close()
        return nil, nil, err
    }
    closer := func() { _ = db.Close() }
    return p, closer, nil
}

func (p *PostgresStore) migrate() error {
    stmts := []string{
        `create table if not exists polls (
            id          text primary key,
            question    text not null,
            is_open     boolean not null default true,
            created_at  timestamptz not null default now()
        )`,
        `create table if not exists poll_options (
            id       text primary key,
            poll_id  text not null references polls(id) on delete cascade,
            label    text not null,
            votes    integer not null default 0,
            unique (poll_id, label)
        )`,
        `create table if not exists poll_voters (
            poll_id  text not null references polls(id) on delete cascade,
            voter_id text not null,
            voted_at timestamptz not null default now(),
            primary key (poll_id, voter_id)
        )`,
        `create index if not exists idx_poll_options_poll on poll_options(poll_id)`,
        `create index if not exists idx_poll_voters_poll on poll_voters(poll_id)`,
    }
    for _, s := range stmts {
        if _, err := p.db.Exec(s); err != nil {
            return err
        }
    }
    return nil
}

func (p *PostgresStore) CheckPollAndOption(pollID, optionID string) error {
    var isOpen bool
    var exists bool
    err := p.db.QueryRow(`select is_open from polls where id=$1`, pollID).Scan(&isOpen)
    if err == sql.ErrNoRows {
        return errors.New("poll not found")
    }
    if err != nil {
        return err
    }
    if !isOpen {
        return errors.New("poll is closed")
    }
    err = p.db.QueryRow(`select exists(select 1 from poll_options where id=$1 and poll_id=$2)`, optionID, pollID).Scan(&exists)
    if err != nil {
        return err
    }
    if !exists {
        return errors.New("option not found in poll")
    }
    return nil
}

func (p *PostgresStore) ApplyVote(v models.VoteRequest) error {
    tx, err := p.db.Begin()
    if err != nil {
        return err
    }
    defer func() { _ = tx.Rollback() }()

    var isOpen bool
    if err := tx.QueryRow(`select is_open from polls where id=$1`, v.PollID).Scan(&isOpen); err != nil {
        if err == sql.ErrNoRows {
            return errors.New("poll not found")
        }
        return err
    }
    if !isOpen {
        return errors.New("poll is closed")
    }
    var optExists bool
    if err := tx.QueryRow(`select exists(select 1 from poll_options where id=$1 and poll_id=$2)`, v.OptionID, v.PollID).Scan(&optExists); err != nil {
        return err
    }
    if !optExists {
        return errors.New("option not found in poll")
    }
    if _, err := tx.Exec(`insert into poll_voters(poll_id, voter_id) values($1,$2)`, v.PollID, v.VoterID); err != nil {
        if strings.Contains(err.Error(), "duplicate key") || strings.Contains(strings.ToLower(err.Error()), "unique") {
            return errors.New("voter has already voted in this poll")
        }
        return err
    }
    if _, err := tx.Exec(`update poll_options set votes = votes + 1 where id=$1`, v.OptionID); err != nil {
        return err
    }
    if err := tx.Commit(); err != nil {
        return err
    }
    return nil
}

func (p *PostgresStore) GetOption(id string) (*models.OptionItem, bool) {
    var opt models.OptionItem
    err := p.db.QueryRow(`select id, label, votes from poll_options where id=$1`, id).Scan(&opt.ID, &opt.Label, &opt.Votes)
    if err == sql.ErrNoRows {
        return nil, false
    }
    if err != nil {
        return nil, false
    }
    return &opt, true
}

func (p *PostgresStore) GetPollSnapshot(id string) (PollSnapshot, bool) {
    var snap PollSnapshot
    err := p.db.QueryRow(`select id, question, is_open from polls where id=$1`, id).Scan(&snap.ID, &snap.Question, &snap.IsOpen)
    if err == sql.ErrNoRows {
        return PollSnapshot{}, false
    }
    if err != nil {
        return PollSnapshot{}, false
    }
    rows, err := p.db.Query(`select id, label, votes from poll_options where poll_id=$1`, id)
    if err == nil {
        defer rows.Close()
        for rows.Next() {
            var o models.OptionItem
            if err := rows.Scan(&o.ID, &o.Label, &o.Votes); err == nil {
                snap.Options = append(snap.Options, o)
            }
        }
    }
    vrows, err := p.db.Query(`select voter_id from poll_voters where poll_id=$1`, id)
    if err == nil {
        defer vrows.Close()
        for vrows.Next() {
            var vid string
            if err := vrows.Scan(&vid); err == nil {
                snap.Voters = append(snap.Voters, vid)
            }
        }
    }
    return snap, true
}

func (p *PostgresStore) ListPollSnapshots() []PollSnapshot {
    rows, err := p.db.Query(`select id from polls`)
    if err != nil {
        return nil
    }
    defer rows.Close()
    ids := []string{}
    for rows.Next() {
        var id string
        if err := rows.Scan(&id); err == nil {
            ids = append(ids, id)
        }
    }
    snaps := make([]PollSnapshot, 0, len(ids))
    for _, id := range ids {
        if snap, ok := p.GetPollSnapshot(id); ok {
            snaps = append(snaps, snap)
        }
    }
    sort.Slice(snaps, func(i, j int) bool {
        if snaps[i].Question == snaps[j].Question {
            return snaps[i].ID < snaps[j].ID
        }
        return snaps[i].Question < snaps[j].Question
    })
    return snaps
}

func (p *PostgresStore) ListOptions(pollID string) []models.OptionItem {
    var rows *sql.Rows
    var err error
    if strings.TrimSpace(pollID) == "" {
        rows, err = p.db.Query(`select id, label, votes from poll_options`)
    } else {
        rows, err = p.db.Query(`select id, label, votes from poll_options where poll_id=$1`, pollID)
    }
    if err != nil {
        return nil
    }
    defer rows.Close()
    out := []models.OptionItem{}
    for rows.Next() {
        var o models.OptionItem
        if err := rows.Scan(&o.ID, &o.Label, &o.Votes); err == nil {
            out = append(out, o)
        }
    }
    sort.Slice(out, func(i, j int) bool {
        if out[i].Label == out[j].Label {
            return out[i].ID < out[j].ID
        }
        return out[i].Label < out[j].Label
    })
    return out
}

func (p *PostgresStore) CreatePoll(id, question string, isOpen bool) error {
    _, err := p.db.Exec(`insert into polls(id, question, is_open) values($1,$2,$3)`, id, question, isOpen)
    if err != nil {
        if strings.Contains(err.Error(), "duplicate key") {
            return errors.New("poll already exists")
        }
        return err
    }
    return nil
}

func (p *PostgresStore) UpdatePoll(id, question string, isOpen bool) error {
    res, err := p.db.Exec(`update polls set question=$1, is_open=$2 where id=$3`, question, isOpen, id)
    if err != nil {
        return err
    }
    n, _ := res.RowsAffected()
    if n == 0 {
        return errors.New("poll not found")
    }
    return nil
}

func (p *PostgresStore) DeletePoll(id string) error {
    res, err := p.db.Exec(`delete from polls where id=$1`, id)
    if err != nil {
        return err
    }
    n, _ := res.RowsAffected()
    if n == 0 {
        return errors.New("poll not found")
    }
    return nil
}

func (p *PostgresStore) AddOption(pollID, optionID, label string) error {
    var tmp string
    if err := p.db.QueryRow(`select id from polls where id=$1`, pollID).Scan(&tmp); err != nil {
        if err == sql.ErrNoRows {
            return errors.New("poll not found")
        }
        return err
    }
    _, err := p.db.Exec(`insert into poll_options(id, poll_id, label) values($1,$2,$3)`, optionID, pollID, label)
    if err != nil {
        if strings.Contains(err.Error(), "duplicate key") {
            return errors.New("option already exists")
        }
        if strings.Contains(strings.ToLower(err.Error()), "unique") {
            return errors.New("option already exists")
        }
        return err
    }
    return nil
}

func (p *PostgresStore) UpdateOption(optionID, label string) error {
    res, err := p.db.Exec(`update poll_options set label=$1 where id=$2`, label, optionID)
    if err != nil {
        return err
    }
    n, _ := res.RowsAffected()
    if n == 0 {
        return errors.New("option not found")
    }
    return nil
}

func (p *PostgresStore) DeleteOption(optionID string) error {
    res, err := p.db.Exec(`delete from poll_options where id=$1`, optionID)
    if err != nil {
        return err
    }
    n, _ := res.RowsAffected()
    if n == 0 {
        return errors.New("option not found")
    }
    return nil
}

func (p *PostgresStore) AddVoter(pollID, voterID string) error {
    var tmp string
    if err := p.db.QueryRow(`select id from polls where id=$1`, pollID).Scan(&tmp); err != nil {
        if err == sql.ErrNoRows {
            return errors.New("poll not found")
        }
        return err
    }
    _, err := p.db.Exec(`insert into poll_voters(poll_id, voter_id) values($1,$2)`, pollID, voterID)
    if err != nil {
        if strings.Contains(strings.ToLower(err.Error()), "duplicate key") || strings.Contains(strings.ToLower(err.Error()), "unique") {
            return errors.New("voter already exists")
        }
        return err
    }
    return nil
}

func (p *PostgresStore) DeleteVoter(pollID, voterID string) error {
    res, err := p.db.Exec(`delete from poll_voters where poll_id=$1 and voter_id=$2`, pollID, voterID)
    if err != nil {
        return err
    }
    n, _ := res.RowsAffected()
    if n == 0 {
        return errors.New("voter not found")
    }
    return nil
}

func (p *PostgresStore) String() string { return fmt.Sprintf("PostgresStore(%p)", p) }
