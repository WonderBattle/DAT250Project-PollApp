package com.DAT250Project.PollApp.model;

import java.util.*;

public class User {

    private UUID id;

    private String username;

    private String email;

    private Set<Poll> createdPolls = new LinkedHashSet<>();

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
