package com.DAT250Project.PollApp.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "votes")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Vote implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private UUID id;

    private Instant publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id", nullable = true)  // allow anonymous votes
    @JsonIgnoreProperties({"votes", "createdPolls"}) // Ignore User's collections
    private User voter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    @JsonIgnoreProperties({"votes", "poll"}) // Break cycles
    private VoteOption option;

    //CONSTRUCTORS
    public Vote(){

    }

    public Vote (User voter, VoteOption option){
        this.voter = voter;
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
    public void setVoter(User voter) {
        this.voter = voter;
    }
    public User getVoter() {
        return voter;
    }

    public UUID getVoterId() {
        if (voter != null) {
            return voter.getId();
        }else{
            return null;
        }
    }

    public void setVoterId(UUID voterId){
        if (this.voter == null) {
            this.voter = new User();  // prevent NPE
        }
        this.voter.setId(voterId);
    }

    public void setOption(VoteOption option) {
        this.option = option;
    }
    public VoteOption getOption() {
        return option;
    }

    public UUID getOptionId(){
        if (option != null){
            return option.getId();
        }else{
            return null;
        }
    }

    public void setOptionId(UUID optionId){
        if (this.option == null) {
            this.option = new VoteOption();
        }
        this.option.setId(optionId);
    }

    //METHODS


}
