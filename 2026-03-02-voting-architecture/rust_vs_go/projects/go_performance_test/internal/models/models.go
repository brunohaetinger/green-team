package models

import "sync"

type OptionItem struct {
	ID    uint32 `json:"id"`
	Label string `json:"label"`
	Votes int    `json:"votes"`
}

type Poll struct {
	ID       uint32                 `json:"id"`
	Question string                 `json:"question"`
	IsOpen   bool                   `json:"is_open"`
	Options  map[uint32]*OptionItem `json:"-"`
	Voters   map[string]struct{}    `json:"-"`
	Mu       sync.Mutex             `json:"-"`
}

type VoteRequest struct {
	PollID   uint32 `json:"poll_id"`
	OptionID uint32 `json:"option_id"`
	VoterID  string `json:"voter_id"`
}
