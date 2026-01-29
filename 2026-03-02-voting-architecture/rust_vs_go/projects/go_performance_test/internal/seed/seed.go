package seed

import (
	"github.com/thiagonasc/poll/internal/models"
	"github.com/thiagonasc/poll/internal/store"
)

func SeedDemo(s store.Store) {
	pollID := uint32(1)
	optA := &models.OptionItem{ID: 1, Label: "Option A", Votes: 0}
	optB := &models.OptionItem{ID: 2, Label: "Option B", Votes: 0}
	optC := &models.OptionItem{ID: 3, Label: "Option C", Votes: 0}
	_ = s.CreatePoll(pollID, "Which option do you prefer?", true)
	_ = s.AddOption(pollID, optA.ID, optA.Label)
	_ = s.AddOption(pollID, optB.ID, optB.Label)
	_ = s.AddOption(pollID, optC.ID, optC.Label)
}
