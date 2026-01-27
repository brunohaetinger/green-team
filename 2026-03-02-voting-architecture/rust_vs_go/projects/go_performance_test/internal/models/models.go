package models

import "sync"

type OptionItem struct {
	ID    string `json:"id"`
	Label string `json:"label"`
	Votes int    `json:"votes"`
}

type Poll struct {
    ID       string                 `json:"id"`
    Question string                 `json:"question"`
    IsOpen   bool                   `json:"is_open"`
    Options  map[string]*OptionItem `json:"-"`
    Voters   map[string]struct{}    `json:"-"`
    Mu       sync.Mutex             `json:"-"`
}

type VoteRequest struct {
	PollID   string `json:"poll_id"`
	OptionID string `json:"option_id"`
	VoterID  string `json:"voter_id"`
}
