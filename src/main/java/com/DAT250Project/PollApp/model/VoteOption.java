package com.DAT250Project.PollApp.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "vote_options")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class VoteOption {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String caption;

    @Column(nullable = false)
    private int presentationOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
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
