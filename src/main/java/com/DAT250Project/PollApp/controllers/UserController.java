package com.DAT250Project.PollApp.controllers;

import com.DAT250Project.PollApp.PollManager;
import com.DAT250Project.PollApp.model.Poll;
import com.DAT250Project.PollApp.model.Vote;
import com.DAT250Project.PollApp.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User management APIs")
public class UserController {

    private PollManager pollManager;

    /**
     * Constructs a UserController with the required PollManager dependency.
     *
     * @param pollManager the service layer responsible for user-related operations
     */
    public UserController(PollManager pollManager) {
        this.pollManager = pollManager;
    }

    /**
     * Creates a new user.
     *
     * @param user the user data to create
     * @return the created user with HTTP 201 status
     */
    @Operation(summary = "Create a new user", description = "Creates a new user account and returns the created user")
    @PostMapping
    public ResponseEntity<User> createUser (@RequestBody User user) {
        User createdUser = pollManager.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);  // CREATED = 201
    }

    /**
     * Retrieves all registered users.
     *
     * @return list of users with HTTP 200 status
     */
    @Operation(summary = "Get all users", description = "Returns a list of all registered users")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(pollManager.getAllUsers()); // OK = 200
    }

    /**
     * Retrieves a user by ID.
     *
     * @param userId UUID of the user
     * @return the user with HTTP 200, or 404 if not found
     */
    @Operation(summary = "Get a user", description = "Get a specific user by its id")
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable UUID userId) {
        User user = pollManager.getUserById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
        return  ResponseEntity.ok(user); // OK = 200
    }

    /**
     * Deletes a user by ID.
     *
     * @param userId UUID of the user to delete
     * @return HTTP 204 if deleted, otherwise 404
     */
    @Operation(summary = "Delete a user", description = "Delete a user by its id")
    @DeleteMapping("/{userId}")
    public ResponseEntity<User> deleteUser(@PathVariable UUID userId) {
        User user = pollManager.deleteUserById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
        return ResponseEntity.noContent().build();  //NO CONTENT = 204
    }

    /**
     * Retrieves all polls created by a specific user.
     *
     * @param userId UUID of the user
     * @return list of polls or 404 if the user does not exist
     */
    @Operation(summary = "Get user's polls", description = "Return a list of the polls created by the user")
    @GetMapping("/{userId}/polls")
    public ResponseEntity<List<Poll>> getUserPolls(@PathVariable UUID userId) {
        if (pollManager.getUserById(userId) == null){
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
        return ResponseEntity.ok(pollManager.getPollsByUser(userId));  //NO CONTENT = 204
    }

    /**
     * Retrieves all votes made by a specific user.
     *
     * @param userId UUID of the user
     * @return a list of votes or 404 if the user does not exist
     */
    @Operation(summary = "Get user's votes", description = "Return a list of the votes that the user has made")
    @GetMapping("/{userId}/votes")
    public ResponseEntity<List<Vote>> getUserVotes(@PathVariable UUID userId) {
        if (pollManager.getUserById(userId) == null){
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
        return ResponseEntity.ok(pollManager.getVotesByUser(userId));  //NO CONTENT = 204
    }

}
