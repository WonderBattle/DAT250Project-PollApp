package com.DAT250Project.PollApp.model;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class VoteOption {

    private UUID id;

    private String caption;

    private int presentationOrder;

    @JsonIdentityReference(alwaysAsId = true)
    private Poll poll;

    private Set<Vote> votes = new LinkedHashSet<>();

    //CONSTRUCTORS
    public VoteOption() {}

    public VoteOption(String caption,  int presentationOrder, Poll poll) {
        this.caption = caption;
        this.presentationOrder = presentationOrder;
        this.poll = poll;
    }

    //SETTERS AND GETTERS
    public void setId(UUID id) {
        this.id = id;
    }
    public UUID getId() {
        return id;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
    public String getCaption() {
        return caption;
    }

    public void setPresentationOrder(int presentationOrder) {
        this.presentationOrder = presentationOrder;
    }
    public int getPresentationOrder() {
        return presentationOrder;
    }

    //Relational setters and getters
    public void setPoll(Poll poll) {
        this.poll = poll;
    }
    public Poll getPoll() {
        return poll;
    }

    public void setVotes(Set<Vote> votes) {
        this.votes = votes;
    }
    public Set<Vote> getVotes() {
        return votes;
    }

    //METHODS



}
