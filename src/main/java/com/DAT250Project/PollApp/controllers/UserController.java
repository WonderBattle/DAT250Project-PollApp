package com.DAT250Project.PollApp.controllers;


import com.DAT250Project.PollApp.PollManager;
import com.DAT250Project.PollApp.model.Poll;
import com.DAT250Project.PollApp.model.Vote;
import com.DAT250Project.PollApp.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/users")
public class UserController {

    private PollManager pollManager;

    //CONSTRUCTOR
    public UserController(PollManager pollManager) {
        this.pollManager = pollManager;
    }

    //Create user
    @PostMapping
    public ResponseEntity<User> createUser (@RequestBody User user) {
        User createdUser = pollManager.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);  // CREATED = 201
    }

    //Get all users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(pollManager.getAllUsers()); // OK = 200
    }

    //Get user by id
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable UUID userId) {
        User user = pollManager.getUserById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
        return  ResponseEntity.ok(user); // OK = 200
    }

    //Delete user by id
    //TODO choose between return an User or a boolean
    @DeleteMapping("/{userId}")
    public ResponseEntity<User> deleteUser(@PathVariable UUID userId) {
        User user = pollManager.deleteUserById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
        return ResponseEntity.noContent().build();  //NO CONTENT = 204
    }

    /*
    public ResponseEntity<Void> deleteUser (@PathVariable UUID userId) {
        boolean deleted = pollManager.deleteUserById(userId);
        if (deleted) {
            return ResponseEntity.noContent().build();  //NO CONTENT = 204
        }else{
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
    }
     */

    //Get user's polls
    @GetMapping("/{userId}/polls")
    public ResponseEntity<List<Poll>> getUserPolls(@PathVariable UUID userId) {
        if (pollManager.getUserById(userId) == null){
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
        return ResponseEntity.ok(pollManager.getPollsByUser(userId));  //NO CONTENT = 204
    }

    //Get user's votes
    @GetMapping("/{userId}/votes")
    public ResponseEntity<List<Vote>> getUserVotes(@PathVariable UUID userId) {
        if (pollManager.getUserById(userId) == null){
            return ResponseEntity.notFound().build();  //NOT FOUND = 404
        }
        return ResponseEntity.ok(pollManager.getVotesByUser(userId));  //NO CONTENT = 204
    }

}
