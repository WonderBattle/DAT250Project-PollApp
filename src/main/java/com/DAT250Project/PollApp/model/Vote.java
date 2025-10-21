package com.DAT250Project.PollApp.model;

import java.time.Instant;
import java.util.*;

public class Vote {

    private UUID id;

    private Instant publishedAt;

    private User castBy;

    private VoteOption option;

    //CONSTRUCTORS
    public Vote(){

    }

    public Vote (User castBy, VoteOption option){
        this.castBy = castBy;
        this.option = option;
        this.publishedAt = Instant.now();
        // CLARA
        /*if(user != null) {  // there could be anonymous votes
            this.user.getMyVotes().add(this);
        }
        this.option.getVotes().add(this);
        */
    }

    //SETTERS AND GETTERS

    public void setId(UUID id) {
        this.id = id;
    }
    public UUID getId() {
        return id;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }
    public Instant getPublishedAt() {
        return publishedAt;
    }

    //Relational setters and getters
    public void setCastBy(User castBy) {
        this.castBy = castBy;
    }
    public User getCastBy() {
        return castBy;
    }

    public void setOption(VoteOption option) {
        this.option = option;
    }
    public VoteOption getOption() {
        return option;
    }

    //METHODS


}
