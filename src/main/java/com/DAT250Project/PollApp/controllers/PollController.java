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
import java.util.Map;
import java.util.UUID;

/**
 * Controller responsible for managing polls and their vote options.
 * Provides REST endpoints for creating, retrieving, updating, and deleting polls,
 * as well as managing vote options associated with them.
 */
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/polls")
@Tag(name = "Polls", description = "Poll management APIs")
public class PollController {

    private final PollManager pollManager;

    /**
     * Constructs the PollController with the required PollManager dependency.
     *
     * @param pollManager the service layer managing poll operations
     */
    public PollController(PollManager pollManager) {
        this.pollManager = pollManager;
    }

    /**
     * Creates a new poll.
     *
     * @param poll the poll data to create
     * @return the created poll wrapped in a ResponseEntity with HTTP 201 status
     */
    @Operation(summary = "Create a new poll", description = "Creates a new poll and returns it")
    @PostMapping
    public ResponseEntity<Poll> createPoll(@RequestBody Poll poll) {
        Poll createdPoll = pollManager.createPoll(poll);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPoll); // CREATED = 201
    }

    /**
     * Retrieves all polls.
     *
     * @return a list of all polls with HTTP 200 status
     */
    @Operation(summary = "Get all polls", description = "Returns a list of all polls")
    @GetMapping
    public ResponseEntity<List<Poll>> getAllPolls() {
        return ResponseEntity.ok(pollManager.getAllPolls()); // OK = 200
    }

    /**
     * Retrieves all public polls.
     *
     * @return a list of public polls with HTTP 200 status
     */
    @Operation(summary = "Get public polls", description = "Returns a list of all the public polls")
    @GetMapping("/public")
    public ResponseEntity<List<Poll>> getPublicPolls() {
        return ResponseEntity.ok(pollManager.getPublicPolls());
    }

    /**
     * Retrieves all private polls belonging to a specific user.
     *
     * @param userId the UUID of the user
     * @return a list of the user's private polls with HTTP 200 status
     */
    @Operation(summary = "Get private polls of an user", description = "Returns a list of all the privates polls from an user")
    @GetMapping("/private/{userId}")
    public ResponseEntity<List<Poll>> getPrivatePolls(@PathVariable UUID userId) {
        return ResponseEntity.ok(pollManager.getPrivatePolls(userId));
    }

    /**
     * Retrieves all polls (public + private) from a specific user.
     *
     * @param userId the UUID of the user
     * @return a list of all polls created by the user with HTTP 200 status
     */
    @Operation(summary = "Get private and public polls of an user", description = "Returns a list of all the polls from an user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Poll>> getUserPolls(@PathVariable UUID userId) {
        return ResponseEntity.ok(pollManager.getPollsByUser(userId));
    }

    /**
     * Retrieves a poll by its ID.
     *
     * @param pollId the UUID of the poll to retrieve
     * @return the poll if found, otherwise 404 NOT FOUND
     */
    @Operation(summary = "Get a poll", description = "Get a poll by its ID")
    @GetMapping("/{pollId}")
    public ResponseEntity<Poll> getPollById(@PathVariable UUID pollId) {
        Poll poll = pollManager.getPollById(pollId);
        if (poll == null) {
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
        return  ResponseEntity.ok(poll); // OK = 200
    }

    /**
     * Updates the privacy setting of a poll.
     *
     * @param pollId   the UUID of the poll to update
     * @param isPublic true if the poll should be public, false if private
     * @param userId   the UUID of the user attempting the update
     * @return the updated poll or 404 NOT FOUND if the poll does not exist
     */
    @Operation(summary = "Update poll privacy status", description = "Update whether a poll is public or private")
    @PutMapping("/{pollId}/privacy")
    public ResponseEntity<Poll> updatePollPrivacy(@PathVariable UUID pollId,
                                                  @RequestParam boolean isPublic,
                                                  @RequestParam UUID userId) {

        Poll updatedPoll = pollManager.updatePollPrivacy(pollId, isPublic, userId); // Add userId here
        if (updatedPoll == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedPoll);
    }

    /**
     * Deletes a poll by its ID.
     *
     * @param pollId the UUID of the poll to delete
     * @return 204 NO CONTENT if deleted, or 404 NOT FOUND if not found
     */
    @Operation(summary = "Delete a poll", description = "Deletes a poll by its ID")
    @DeleteMapping("/{pollId}")
    public ResponseEntity<Poll> deletePoll(@PathVariable UUID pollId) {
        Poll poll = pollManager.deletePollById(pollId);
        if (poll == null) {
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
        return ResponseEntity.noContent().build();  //NO CONTENT = 204
    }

    /**
     * Adds a vote option to a poll.
     *
     * @param pollId the UUID of the poll
     * @param option the vote option to add
     * @return the created vote option with HTTP 201 status, or 404 if poll not found
     */
    @Operation(summary = "Add an option", description = "Add an option to a poll by ID")
    @PostMapping("/{pollId}/options")
    public ResponseEntity<VoteOption> addOption(@PathVariable UUID pollId, @RequestBody VoteOption option) {
        if(pollManager.getPollById(pollId) == null){
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
        VoteOption created = pollManager.addOptionToPoll(pollId, option);
        return ResponseEntity.status(HttpStatus.CREATED).body(created); // CREATED = 201
    }

    /**
     * Retrieves an option by its ID.
     *
     * @param pollId   the UUID of the poll
     * @param optionId the UUID of the option
     * @return the vote option or 404 NOT FOUND if not found
     */
    @Operation(summary = "Get an option by id", description = "Get a option by its ID")
    @GetMapping("/{pollId}/options/{optionId}")
    public ResponseEntity<VoteOption> getOptionById(@PathVariable UUID pollId, @PathVariable UUID optionId) {
        VoteOption voteOption = pollManager.getOptionById(optionId);
        if (voteOption == null) {
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
        return  ResponseEntity.ok(voteOption); // OK = 200
    }

    /**
     * Deletes a vote option from a poll.
     *
     * @param pollId   the UUID of the poll
     * @param optionId the UUID of the option
     * @return 204 NO CONTENT if deleted, 404 if not found
     */
    @Operation(summary = "Delete a vote option", description = "Deletes a vote option by its ID")
    @DeleteMapping("/{pollId}/options/{optionId}")
    public ResponseEntity<VoteOption> deleteOption(@PathVariable UUID pollId, @PathVariable UUID optionId) {
        VoteOption voteOption = pollManager.deleteOptionById(optionId);
        if (voteOption == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves all vote options for a poll.
     *
     * @param pollId the UUID of the poll
     * @return list of vote options or 404 if poll does not exist
     */
    @Operation(summary = "Get all options", description = "Get all options of a poll by its ID")
    @GetMapping("/{pollId}/options")
    public ResponseEntity<List<VoteOption>> getAllOptions (@PathVariable UUID pollId) {
        if (pollManager.getPollById(pollId)==null){
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
        return ResponseEntity.ok(pollManager.getAllOptionsByPoll(pollId)); // OK = 200
    }

    /**
     * Retrieves vote count results for each option in a poll.
     *
     * @param pollId the UUID of the poll
     * @return a map of optionId to vote count, or 404 if poll not found
     */
    @Operation(summary = "Get vote counts per option", description = "Returns vote counts for each option in a poll")
    @GetMapping("/{pollId}/results")
    public ResponseEntity<Map<UUID, Long>> getPollResults(@PathVariable UUID pollId) {
        Poll poll = pollManager.getPollById(pollId);
        if (poll == null) return ResponseEntity.notFound().build();

        Map<UUID, Long> results = pollManager.countVotesForPoll(pollId);
        return ResponseEntity.ok(results);
    }

}
