package com.DAT250Project.PollApp.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.*;

/**
 * Represents an application user who may create polls and cast votes.
 */
@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    /** Unique identifier for the user. */
    @Id
    @GeneratedValue
    private UUID id;

    /** Username of the user (unique). */
    @Column(unique = true, nullable = false)
    private String username;

    /** Email address (unique). */
    @Column(unique = true, nullable = false)
    private String email;

    /** Hashed password (write-only in JSON). */
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /** Role of the user (default: ROLE_USER). */
    @Column(nullable = false)
    private String role = "ROLE_USER";

    /** Polls created by this user. */
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Poll> createdPolls = new LinkedHashSet<>();

    /** Votes made by this user. */
    @OneToMany(mappedBy = "voter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Vote> votes = new LinkedHashSet<>();

    /**
     * Constructor with required fields.
     */
    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    /** Default constructor. */
    public User() {}

    /** Sets the user ID. */
    public void setId(UUID id) { this.id = id; }

    /** Returns the user ID. */
    public UUID getId() { return id; }

    /** Sets the username. */
    public void setUsername(String username) { this.username = username; }

    /** Returns the username. */
    public String getUsername() { return username; }

    /** Sets the user's email. */
    public void setEmail(String email) { this.email = email; }

    /** Returns the user's email. */
    public String getEmail() { return email; }

    /** Sets the password. */
    public void setPassword(String password) { this.password = password; }

    /** Returns the hashed password. */
    public String getPassword() { return password; }

    /** Sets the user's role. */
    public void setRole(String role) { this.role = role; }

    /** Returns the user's role. */
    public String getRole() { return role; }

    /** Sets the user's created polls. */
    public void setCreatedPolls(Set<Poll> createdPolls) { this.createdPolls = createdPolls; }

    /** Returns the polls created by the user. */
    public Set<Poll> getCreatedPolls() { return createdPolls; }

    /** Sets the user's votes. */
    public void setVotes(Set<Vote> votes) { this.votes = votes; }

    /** Returns the user's cast votes. */
    public Set<Vote> getVotes() { return votes; }

    /**
     * Creates a poll for this user.
     */
    public Poll createPoll(String question) {
        Poll poll = new Poll();
        poll.setQuestion(question);
        poll.setCreatedBy(this);
        this.createdPolls.add(poll);
        return poll;
    }

    /**
     * Casts a vote for a specific vote option.
     */
    public Vote voteFor(VoteOption option) {
        Vote vote = new Vote(this, option);
        this.votes.add(vote);
        return vote;
    }
}
