package com.DAT250Project.PollApp.controllers;


import com.DAT250Project.PollApp.PollManager;
import com.DAT250Project.PollApp.model.Vote;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/votes")
public class VoteController {

    private PollManager pollManager;

    //CONSTRUCTOR
    public VoteController(PollManager pollManager) {
        this.pollManager = pollManager;
    }

    //Create a vote
    @PostMapping("/polls/{pollId}/votes")
    public ResponseEntity<Vote> createVote(@PathVariable Long pollId, @RequestBody Vote voteRequest) {
        Vote vote = pollManager.createVote(pollId, voteRequest.getVoterId(), voteRequest.getOptionId());
        return ResponseEntity.created(URI.create("votes/" + vote.getId())).body(vote);
    }

    //Update a vote


    //Get all votes


    //Get vote by id


    //Delete a vote
}
