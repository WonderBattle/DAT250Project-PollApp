package com.DAT250Project.PollApp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "votes")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class Vote {

    @Id
    @GeneratedValue
    private UUID id;

    private Instant publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id", nullable = false)
    private User voter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
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
