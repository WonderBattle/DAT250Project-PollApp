package com.DAT250Project.PollApp.controllers;

import com.DAT250Project.PollApp.PollManager;
import com.DAT250Project.PollApp.model.Poll;
import com.DAT250Project.PollApp.model.VoteOption;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

//PollController — manages polls and their options.
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/polls")
@Tag(name = "Polls", description = "Poll management APIs")
public class PollController {

    private final PollManager pollManager;

    //CONSTRUCTOR
    public PollController(PollManager pollManager) {
        this.pollManager = pollManager;
    }

    //Create a poll
    @Operation(summary = "Create a new poll", description = "Creates a new poll and returns it")
    @PostMapping
    public ResponseEntity<Poll> createPoll(@RequestBody Poll poll) {
        Poll createdPoll = pollManager.createPoll(poll);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPoll); // CREATED = 201
    }

    //Get all polls
    @Operation(summary = "Get all polls", description = "Returns a list of all polls")
    @GetMapping
    public ResponseEntity<List<Poll>> getAllPolls() {
        return ResponseEntity.ok(pollManager.getAllPolls()); // OK = 200
    }

    // Get all public polls (visible to everyone)
    @GetMapping("/public")
    public ResponseEntity<List<Poll>> getPublicPolls() {
        return ResponseEntity.ok(pollManager.getPublicPolls());
    }

    // Get all private polls of a specific user
    @GetMapping("/private/{userId}")
    public ResponseEntity<List<Poll>> getPrivatePolls(@PathVariable UUID userId) {
        return ResponseEntity.ok(pollManager.getPrivatePolls(userId));
    }

    //Get poll by id
    @Operation(summary = "Get a poll", description = "Get a poll by its ID")
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
    @Operation(summary = "Delete a poll", description = "Deletes a poll by its ID")
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
    @Operation(summary = "Add an option", description = "Add an option to a poll by ID")
    @PostMapping("/{pollId}/options")
    public ResponseEntity<VoteOption> addOption(@PathVariable UUID pollId, @RequestBody VoteOption option) {
        if(pollManager.getPollById(pollId) == null){
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
        VoteOption created = pollManager.addOptionToPoll(pollId, option);
        return ResponseEntity.status(HttpStatus.CREATED).body(created); // CREATED = 201
    }

    //Get poll by id
    @Operation(summary = "Get an option by id", description = "Get a option by its ID")
    @GetMapping("/{pollId}/options/{optionId}")
    public ResponseEntity<VoteOption> getOptionById(@PathVariable UUID pollId, @PathVariable UUID optionId) {
        VoteOption voteOption = pollManager.getOptionById(optionId);
        if (voteOption == null) {
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
        return  ResponseEntity.ok(voteOption); // OK = 200
    }

    //Delete a vote option of a poll
    @Operation(summary = "Delete a vote option", description = "Deletes a vote option by its ID")
    @DeleteMapping("/{pollId}/options/{optionId}")
    public ResponseEntity<VoteOption> deleteOption(@PathVariable UUID pollId, @PathVariable UUID optionId) {
        VoteOption voteOption = pollManager.deleteOptionById(optionId);
        if (voteOption == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    //Get all vote options in a poll
    @Operation(summary = "Get all options", description = "Get all options of a poll by its ID")
    @GetMapping("/{pollId}/options")
    public ResponseEntity<List<VoteOption>> getAllOptions (@PathVariable UUID pollId) {
        if (pollManager.getPollById(pollId)==null){
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
        return ResponseEntity.ok(pollManager.getAllOptionsByPoll(pollId)); // OK = 200
    }

}
