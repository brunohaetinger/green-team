package store

import (
	"errors"
	"os"
	"sort"
	"strings"
	"sync"

	"github.com/thiagonasc/poll/internal/models"
)

type Store interface {
	CheckPollAndOption(pollID uint32, optionID uint32) error
	ApplyVote(v models.VoteRequest) error

	GetOption(id uint32) (*models.OptionItem, bool)
	GetPollSnapshot(id uint32) (PollSnapshot, bool)
	ListPollSnapshots() []PollSnapshot
	ListOptions(pollID uint32) []models.OptionItem

	CreatePoll(id uint32, question string, isOpen bool) error
	UpdatePoll(id uint32, question string, isOpen bool) error
	DeletePoll(id uint32) error

	AddOption(pollID uint32, optionID uint32, label string) error
	UpdateOption(optionID uint32, label string) error
	DeleteOption(optionID uint32) error

	AddVoter(pollID uint32, voterID string) error
	DeleteVoter(pollID uint32, voterID string) error
}

type MemoryStore struct {
	mu          sync.RWMutex
	polls       map[uint32]*models.Poll
	optionIndex map[uint32]*models.OptionItem
	skipVoter   bool
}

func New() *MemoryStore {
	skip := strings.TrimSpace(os.Getenv("SKIP_VOTER_TRACK")) == "1"
	return &MemoryStore{
		polls:       make(map[uint32]*models.Poll),
		optionIndex: make(map[uint32]*models.OptionItem),
		skipVoter:   skip,
	}
}

func (s *MemoryStore) AddPoll(p *models.Poll) {
	s.mu.Lock()
	defer s.mu.Unlock()
	if p.Options == nil {
		p.Options = make(map[uint32]*models.OptionItem)
	}
	if p.Voters == nil {
		p.Voters = make(map[string]struct{})
	}
	s.polls[p.ID] = p
	for id, opt := range p.Options {
		s.optionIndex[id] = opt
	}
}

func (s *MemoryStore) GetOption(id uint32) (*models.OptionItem, bool) {
	s.mu.RLock()
	defer s.mu.RUnlock()
	o, ok := s.optionIndex[id]
	return o, ok
}

func (s *MemoryStore) CheckPollAndOption(pollID, optionID uint32) error {
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
	// Fast path: locate poll and option under read lock, then mutate under per-poll lock.
	s.mu.RLock()
	p, ok := s.polls[v.PollID]
	if !ok {
		s.mu.RUnlock()
		return errors.New("poll not found")
	}
	opt, ok := p.Options[v.OptionID]
	if !ok {
		s.mu.RUnlock()
		return errors.New("option not found in poll")
	}
	s.mu.RUnlock()

	p.Mu.Lock()
	defer p.Mu.Unlock()
	if !p.IsOpen {
		return errors.New("poll is closed")
	}
	if !s.skipVoter {
		if _, voted := p.Voters[v.VoterID]; voted {
			return errors.New("voter has already voted in this poll")
		}
		p.Voters[v.VoterID] = struct{}{}
	}
	opt.Votes++
	return nil
}

type PollSnapshot struct {
	ID       uint32
	Question string
	IsOpen   bool
	Options  []models.OptionItem
	Voters   []string
}

func (s *MemoryStore) GetPollSnapshot(id uint32) (PollSnapshot, bool) {
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
	keys := make([]uint32, 0, len(s.polls))
	for id := range s.polls {
		keys = append(keys, id)
	}
	sort.Slice(keys, func(i, j int) bool { return keys[i] < keys[j] })
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

func (s *MemoryStore) ListOptions(pollID uint32) []models.OptionItem {
	s.mu.RLock()
	defer s.mu.RUnlock()
	out := []models.OptionItem{}
	if pollID == 0 {
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

func (s *MemoryStore) CreatePoll(id uint32, question string, isOpen bool) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	if _, exists := s.polls[id]; exists {
		return errors.New("poll already exists")
	}
	p := &models.Poll{ID: id, Question: question, IsOpen: isOpen, Options: map[uint32]*models.OptionItem{}, Voters: map[string]struct{}{}}
	s.polls[id] = p
	return nil
}

func (s *MemoryStore) UpdatePoll(id uint32, question string, isOpen bool) error {
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

func (s *MemoryStore) DeletePoll(id uint32) error {
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

func (s *MemoryStore) AddOption(pollID uint32, optionID uint32, label string) error {
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

func (s *MemoryStore) UpdateOption(optionID uint32, label string) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	opt, ok := s.optionIndex[optionID]
	if !ok {
		return errors.New("option not found")
	}
	opt.Label = label
	return nil
}

func (s *MemoryStore) DeleteOption(optionID uint32) error {
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

func (s *MemoryStore) AddVoter(pollID uint32, voterID string) error {
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

func (s *MemoryStore) DeleteVoter(pollID uint32, voterID string) error {
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
