package seed

import (
    "github.com/thiagonasc/poll/internal/models"
    "github.com/thiagonasc/poll/internal/store"
)

func SeedDemo(s store.Store) {
    pollID := "11111111-1111-1111-1111-111111111111"
    optA := &models.OptionItem{ID: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", Label: "Option A", Votes: 0}
    optB := &models.OptionItem{ID: "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb", Label: "Option B", Votes: 0}
    optC := &models.OptionItem{ID: "cccccccc-cccc-cccc-cccc-cccccccccccc", Label: "Option C", Votes: 0}
    _ = s.CreatePoll(pollID, "Which option do you prefer?", true)
    _ = s.AddOption(pollID, optA.ID, optA.Label)
    _ = s.AddOption(pollID, optB.ID, optB.Label)
    _ = s.AddOption(pollID, optC.ID, optC.Label)
}
