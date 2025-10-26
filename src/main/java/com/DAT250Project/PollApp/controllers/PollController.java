package com.DAT250Project.PollApp.controllers;

import com.DAT250Project.PollApp.PollManager;
import com.DAT250Project.PollApp.model.Poll;
import com.DAT250Project.PollApp.model.VoteOption;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

//PollController — manages polls and their options.
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/polls")
public class PollController {

    private final PollManager pollManager;

    //CONSTRUCTOR
    public PollController(PollManager pollManager) {
        this.pollManager = pollManager;
    }

    //Create a poll
    @PostMapping
    public ResponseEntity<Poll> createPoll(@RequestBody Poll poll) {
        Poll createdPoll = pollManager.createPoll(poll);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPoll); // CREATED = 201
    }

    //Get all polls
    @GetMapping
    public ResponseEntity<List<Poll>> getAllPolls() {
        return ResponseEntity.ok(pollManager.getAllPolls()); // OK = 200
    }

    //Get poll by id
    @GetMapping("/{pollId}")
    public ResponseEntity<Poll> getPollById(@PathVariable UUID pollId) {
        Poll poll = pollManager.getPollById(pollId);
        if (poll == null) {
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
        return  ResponseEntity.ok(poll); // OK = 200
    }

    //Delete a poll by id
    //TODO choose between return a Poll or a boolean
    @DeleteMapping("/{pollId}")
    public ResponseEntity<Poll> deletePoll(@PathVariable UUID pollId) {
        Poll poll = pollManager.deletePollById(pollId);
        if (poll == null) {
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
        return ResponseEntity.noContent().build();  //NO CONTENT = 204
    }

    /*
    public ResponseEntity<Void> deletePoll(@PathVariable UUID pollId) {
        boolean deleted = pollManager.deletePollById(pollId);
        if (deleted) {
            return ResponseEntity.noContent().build();  //NO CONTENT = 204
        }else{
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
    }
     */

    //Add a vote option to a poll
    @PostMapping("/{pollId}/options")
    public ResponseEntity<VoteOption> addOption(@PathVariable UUID pollId, @RequestBody VoteOption option) {
        if(pollManager.getPollById(pollId) == null){
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
        VoteOption created = pollManager.addOptionToPoll(pollId, option);
        return ResponseEntity.status(HttpStatus.CREATED).body(created); // CREATED = 201
    }

    //Get all vote options in a poll
    @GetMapping("/{pollId}/options")
    public ResponseEntity<List<VoteOption>> getAllOptions (@PathVariable UUID pollId) {
        if (pollManager.getPollById(pollId)==null){
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
        return ResponseEntity.ok(pollManager.getAllOptionsByPoll(pollId)); // OK = 200
    }

}
