package store

import (
    "errors"
    "sort"
    "sync"
    "strings"

    "github.com/thiagonasc/poll/internal/models"
)

type Store interface {
    CheckPollAndOption(pollID, optionID string) error
    ApplyVote(v models.VoteRequest) error

    GetOption(id string) (*models.OptionItem, bool)
    GetPollSnapshot(id string) (PollSnapshot, bool)
    ListPollSnapshots() []PollSnapshot
    ListOptions(pollID string) []models.OptionItem

    CreatePoll(id, question string, isOpen bool) error
    UpdatePoll(id, question string, isOpen bool) error
    DeletePoll(id string) error

    AddOption(pollID, optionID, label string) error
    UpdateOption(optionID, label string) error
    DeleteOption(optionID string) error

    AddVoter(pollID, voterID string) error
    DeleteVoter(pollID, voterID string) error
}

type MemoryStore struct {
    mu          sync.RWMutex
    polls       map[string]*models.Poll
    optionIndex map[string]*models.OptionItem
}

func New() *MemoryStore {
    return &MemoryStore{
        polls:       make(map[string]*models.Poll),
        optionIndex: make(map[string]*models.OptionItem),
    }
}

func (s *MemoryStore) AddPoll(p *models.Poll) {
    s.mu.Lock()
    defer s.mu.Unlock()
    if p.Options == nil {
        p.Options = make(map[string]*models.OptionItem)
    }
    if p.Voters == nil {
        p.Voters = make(map[string]struct{})
    }
    s.polls[p.ID] = p
    for id, opt := range p.Options {
        s.optionIndex[id] = opt
    }
}

func (s *MemoryStore) GetOption(id string) (*models.OptionItem, bool) {
    s.mu.RLock()
    defer s.mu.RUnlock()
    o, ok := s.optionIndex[id]
    return o, ok
}

func (s *MemoryStore) CheckPollAndOption(pollID, optionID string) error {
    s.mu.RLock()
    defer s.mu.RUnlock()
    p, ok := s.polls[pollID]
    if !ok {
        return errors.New("poll not found")
    }
    if !p.IsOpen {
        return errors.New("poll is closed")
    }
    if _, ok := p.Options[optionID]; !ok {
        return errors.New("option not found in poll")
    }
    return nil
}

func (s *MemoryStore) ApplyVote(v models.VoteRequest) error {
    s.mu.Lock()
    defer s.mu.Unlock()
    p, ok := s.polls[v.PollID]
    if !ok {
        return errors.New("poll not found")
    }
    if !p.IsOpen {
        return errors.New("poll is closed")
    }
    opt, ok := p.Options[v.OptionID]
    if !ok {
        return errors.New("option not found in poll")
    }
    if _, voted := p.Voters[v.VoterID]; voted {
        return errors.New("voter has already voted in this poll")
    }
    opt.Votes++
    p.Voters[v.VoterID] = struct{}{}
    return nil
}

type PollSnapshot struct {
    ID       string
    Question string
    IsOpen   bool
    Options  []models.OptionItem
    Voters   []string
}

func (s *MemoryStore) GetPollSnapshot(id string) (PollSnapshot, bool) {
    s.mu.RLock()
    defer s.mu.RUnlock()
    p, ok := s.polls[id]
    if !ok {
        return PollSnapshot{}, false
    }
    snap := PollSnapshot{ID: p.ID, Question: p.Question, IsOpen: p.IsOpen}
    opts := make([]models.OptionItem, 0, len(p.Options))
    for _, o := range p.Options {
        opts = append(opts, models.OptionItem{ID: o.ID, Label: o.Label, Votes: o.Votes})
    }
    sort.Slice(opts, func(i, j int) bool { return opts[i].Label < opts[j].Label })
    snap.Options = opts
    voters := make([]string, 0, len(p.Voters))
    for v := range p.Voters {
        voters = append(voters, v)
    }
    sort.Strings(voters)
    snap.Voters = voters
    return snap, true
}

func (s *MemoryStore) ListPollSnapshots() []PollSnapshot {
    s.mu.RLock()
    defer s.mu.RUnlock()
    snaps := make([]PollSnapshot, 0, len(s.polls))
    keys := make([]string, 0, len(s.polls))
    for id := range s.polls {
        keys = append(keys, id)
    }
    sort.Strings(keys)
    for _, id := range keys {
        p := s.polls[id]
        snap := PollSnapshot{ID: p.ID, Question: p.Question, IsOpen: p.IsOpen}
        opts := make([]models.OptionItem, 0, len(p.Options))
        for _, o := range p.Options {
            opts = append(opts, models.OptionItem{ID: o.ID, Label: o.Label, Votes: o.Votes})
        }
        sort.Slice(opts, func(i, j int) bool { return opts[i].Label < opts[j].Label })
        snap.Options = opts
        voters := make([]string, 0, len(p.Voters))
        for v := range p.Voters {
            voters = append(voters, v)
        }
        sort.Strings(voters)
        snap.Voters = voters
        snaps = append(snaps, snap)
    }
    sort.Slice(snaps, func(i, j int) bool {
        if snaps[i].Question == snaps[j].Question {
            return snaps[i].ID < snaps[j].ID
        }
        return snaps[i].Question < snaps[j].Question
    })
    return snaps
}

func (s *MemoryStore) ListOptions(pollID string) []models.OptionItem {
    s.mu.RLock()
    defer s.mu.RUnlock()
    out := []models.OptionItem{}
    if strings.TrimSpace(pollID) == "" {
        for _, o := range s.optionIndex {
            out = append(out, models.OptionItem{ID: o.ID, Label: o.Label, Votes: o.Votes})
        }
    } else {
        if p, ok := s.polls[pollID]; ok {
            for _, o := range p.Options {
                out = append(out, models.OptionItem{ID: o.ID, Label: o.Label, Votes: o.Votes})
            }
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

func (s *MemoryStore) CreatePoll(id, question string, isOpen bool) error {
    s.mu.Lock()
    defer s.mu.Unlock()
    if _, exists := s.polls[id]; exists {
        return errors.New("poll already exists")
    }
    p := &models.Poll{ID: id, Question: question, IsOpen: isOpen, Options: map[string]*models.OptionItem{}, Voters: map[string]struct{}{}}
    s.polls[id] = p
    return nil
}

func (s *MemoryStore) UpdatePoll(id, question string, isOpen bool) error {
    s.mu.Lock()
    defer s.mu.Unlock()
    p, ok := s.polls[id]
    if !ok {
        return errors.New("poll not found")
    }
    p.Question = question
    p.IsOpen = isOpen
    return nil
}

func (s *MemoryStore) DeletePoll(id string) error {
    s.mu.Lock()
    defer s.mu.Unlock()
    p, ok := s.polls[id]
    if !ok {
        return errors.New("poll not found")
    }
    for oid := range p.Options {
        delete(s.optionIndex, oid)
    }
    delete(s.polls, id)
    return nil
}

func (s *MemoryStore) AddOption(pollID, optionID, label string) error {
    s.mu.Lock()
    defer s.mu.Unlock()
    p, ok := s.polls[pollID]
    if !ok {
        return errors.New("poll not found")
    }
    if _, exists := p.Options[optionID]; exists {
        return errors.New("option already exists")
    }
    opt := &models.OptionItem{ID: optionID, Label: label, Votes: 0}
    p.Options[optionID] = opt
    s.optionIndex[optionID] = opt
    return nil
}

func (s *MemoryStore) UpdateOption(optionID, label string) error {
    s.mu.Lock()
    defer s.mu.Unlock()
    opt, ok := s.optionIndex[optionID]
    if !ok {
        return errors.New("option not found")
    }
    opt.Label = label
    return nil
}

func (s *MemoryStore) DeleteOption(optionID string) error {
    s.mu.Lock()
    defer s.mu.Unlock()
    opt, ok := s.optionIndex[optionID]
    if !ok {
        return errors.New("option not found")
    }
    for _, p := range s.polls {
        if _, ok := p.Options[optionID]; ok {
            delete(p.Options, optionID)
            break
        }
    }
    delete(s.optionIndex, optionID)
    _ = opt
    return nil
}

func (s *MemoryStore) AddVoter(pollID, voterID string) error {
    s.mu.Lock()
    defer s.mu.Unlock()
    p, ok := s.polls[pollID]
    if !ok {
        return errors.New("poll not found")
    }
    if p.Voters == nil {
        p.Voters = map[string]struct{}{}
    }
    if _, exists := p.Voters[voterID]; exists {
        return errors.New("voter already exists")
    }
    p.Voters[voterID] = struct{}{}
    return nil
}

func (s *MemoryStore) DeleteVoter(pollID, voterID string) error {
    s.mu.Lock()
    defer s.mu.Unlock()
    p, ok := s.polls[pollID]
    if !ok {
        return errors.New("poll not found")
    }
    if _, exists := p.Voters[voterID]; !exists {
        return errors.New("voter not found")
    }
    delete(p.Voters, voterID)
    return nil
}
