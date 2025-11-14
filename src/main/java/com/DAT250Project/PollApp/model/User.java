package com.DAT250Project.PollApp.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    //DONE: revise if we want unique or not - We want unique, important for security
    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    // ---------- NEW : Security -------------------
    // password (BCrypt hashed) - do NOT expose it in API responses
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // don't serialize back to client
    //@JsonProperty(access = WRITE_ONLY) ensures the password can be set from requests but not sent back in responses.
    private String password;

    // role - simple string; default "ROLE_USER" - Usefull if later we want to change funcionalities like you can vote like anonymus
    @Column(nullable = false)
    private String role = "ROLE_USER";

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Completely ignore this collection in JSON
    private Set<Poll> createdPolls = new LinkedHashSet<>();

    @OneToMany(mappedBy = "voter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Completely ignore this collection
    private Set<Vote> votes = new LinkedHashSet<>();

    //CONSTRUCTORS
    public User(String username, String email) {
        this.username = username;
        this.email = email;
        //this.createdPolls = new LinkedHashSet<>();
    }

    public User(){}

    //SETTERS AND GETTERS

    public void setId(UUID id) {
        this.id = id;
    }
    public UUID getId() {
        return id;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public String getUsername() {
        return username;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getEmail() {
        return email;
    }

    public void setPassword(String password) { this.password = password; }
    public String getPassword() { return password; }

    public void setRole(String role) { this.role = role; }
    public String getRole() { return role; }

    //Relational setters and getters
    public void setCreatedPolls(Set<Poll> createdPolls) {
        this.createdPolls = createdPolls;
    }
    public Set<Poll> getCreatedPolls() {
        return createdPolls;
    }

    public void setVotes(Set<Vote> votes) {
        this.votes = votes;
    }
    public Set<Vote> getVotes() {
        return votes;
    }

    //METHODS: An User can:
    // 1. Create a Poll -> createPoll(String question)
    // 2. Vote in a Poll -> voteFor()

    // 1. Create a poll
    //TODO choose a method: using default constructor or parameter constructor
    /*public Poll createPoll(String question) {
        Poll poll = new Poll(question, this);
        this.createdPolls.add(poll);
        poll.setCreatedBy(this);
        return poll;
    */

    public Poll createPoll(String question) {
        Poll poll = new Poll();
        poll.setQuestion(question);
        poll.setCreatedBy(this);
        this.createdPolls.add(poll);
        //poll.setPublishedAt(Instant.now()); CLARA
        //poll.setValidUntil(Instant.now().plus(30, ChronoUnit.DAYS)); CLARA
        return poll;
    }

    // 2. Vote in a Poll
    //TODO choose which Vote constructor use
    public Vote voteFor(VoteOption option) {
        Vote vote = new Vote(this, option);
        this.votes.add(vote);
        return vote;
    }

    /*
    public Vote voteFor(VoteOption option) {
        Poll poll = option.getPoll();
        Vote vote = new Vote(this, poll, option);
        this.votes.add(vote);
        return vote;
    }
     */


}
