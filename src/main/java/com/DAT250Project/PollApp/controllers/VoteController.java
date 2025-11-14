package com.DAT250Project.PollApp.controllers;


import com.DAT250Project.PollApp.PollManager;
import com.DAT250Project.PollApp.model.Poll;
import com.DAT250Project.PollApp.model.User;
import com.DAT250Project.PollApp.model.Vote;
import com.DAT250Project.PollApp.model.VoteOption;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping
@Tag(name = "Votes", description = "Vote management APIs")
public class VoteController {

    private PollManager pollManager;

    //CONSTRUCTOR
    public VoteController(PollManager pollManager) {
        this.pollManager = pollManager;
    }

    //Create a vote
    @Operation(summary = "Create a new vote", description = "Creates a new vote and returns it")
    @PostMapping("/polls/{pollId}/votes")
    public ResponseEntity<Vote> createVote(@PathVariable UUID pollId, @RequestBody Vote voteRequest) {
        //check if the relationship is correct
        if (!pollManager.optionBelongsToPoll(voteRequest.getOptionId(), pollId)) {
            return ResponseEntity.badRequest().build(); // invalid relationship  BAD REQUEST = 400
        }
        Vote createdVote = pollManager.createVote(pollId, voteRequest.getVoterId(), voteRequest.getOptionId());
        //only one vote per poll per user
        if (createdVote == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409 USER ALREADY VOTED
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVote);
    }

    //Update a vote
    @Operation(summary = "Update a vote", description = "Change the actual vote in the poll by another vote")
    @PutMapping("/polls/{pollId}/votes")
    public ResponseEntity<Vote> updateVote(@PathVariable UUID pollId, @RequestBody Vote voteRequest) {
        Vote updatedVote = pollManager.updateVote(pollId, voteRequest.getVoterId(), voteRequest.getOptionId());
        if (updatedVote == null) {
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
        return ResponseEntity.ok(updatedVote);  //OK = 200
    }

    //Get all votes
    @Operation(summary = "Get all votes", description = "Returns a list of all votes")
    @GetMapping("/votes")
    public ResponseEntity<List<Vote>> getAllVotes() {
        return ResponseEntity.ok(pollManager.getAllVotes()); // OK = 200
    }

    //Get all votes by option
    @Operation(summary = "Get the votes of an option", description = "Returns a list of the votes made in a specific option")
    @GetMapping("/{optionId}/votes")
    public ResponseEntity<List<Vote>> getVotesByOptionId(@PathVariable UUID optionId) {
        VoteOption option = pollManager.getOptionById(optionId);
        if (option == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pollManager.getVotesByOption(optionId));
    }

    //Get all votes by poll
    @Operation(summary = "Get the votes of a poll", description = "Returns a list of the votes made in a specific poll")
    @GetMapping("/polls/{pollId}/votes")
    public  ResponseEntity<List<Vote>> getVotesByPollId(@PathVariable UUID pollId) {
        Poll poll = pollManager.getPollById(pollId);
        if (poll == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pollManager.getVotesByPoll(pollId));
    }

    //Get vote by id
    @Operation(summary = "Get a vote", description = "Get a specific vote by its id")
    @GetMapping("/votes/{voteId}")
    public ResponseEntity<Vote> getVoteById(@PathVariable UUID voteId) {
        Vote vote = pollManager.getVoteById(voteId);
        if (vote == null) {
            return ResponseEntity.notFound().build();
        }
        return  ResponseEntity.ok(vote);
    }

    //Delete a vote
    //TODO choose between return a Vote or a boolean
    @Operation(summary = "Remove a vote", description = "Deletes a user's vote for a given vote option")
    @DeleteMapping("/votes/{voteId}")
    public ResponseEntity<Vote> deleteVote(@PathVariable UUID voteId) {
        Vote vote = pollManager.deleteVoteById(voteId);
        if (vote == null) {
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
        return ResponseEntity.noContent().build();  //NO CONTENT = 204
    }

    /*
    public ResponseEntity<Void> deleteVote (@PathVariable UUID voteId) {
        boolean deleted = pollManager.deleteVoteById(userId);
        if (deleted) {
            return ResponseEntity.noContent().build();  //NO CONTENT = 204
        }else{
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
    }
     */
}
