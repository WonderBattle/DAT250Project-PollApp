package com.DAT250Project.PollApp.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a selectable option within a poll.
 */
@Entity
@Table(name = "vote_options")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class VoteOption {

    /** Unique identifier for the vote option. */
    @Id
    @GeneratedValue
    private UUID id;

    /** The text of the option. */
    @Column(nullable = false)
    private String caption;

    /** Order of appearance in the poll. */
    @Column(nullable = false)
    private int presentationOrder;

    /** Poll to which this option belongs. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    @JsonIgnoreProperties("options")
    private Poll poll;

    /** Votes that selected this option. */
    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("option")
    private Set<Vote> votes = new LinkedHashSet<>();

    /** Default constructor. */
    public VoteOption() {}

    /**
     * Constructor for a vote option.
     */
    public VoteOption(String caption, int presentationOrder, Poll poll) {
        this.caption = caption;
        this.presentationOrder = presentationOrder;
        this.poll = poll;
    }

    /** Sets the option ID. */
    public void setId(UUID id) { this.id = id; }

    /** Returns the option ID. */
    public UUID getId() { return id; }

    /** Sets the caption. */
    public void setCaption(String caption) { this.caption = caption; }

    /** Returns the caption. */
    public String getCaption() { return caption; }

    /** Sets the presentation order. */
    public void setPresentationOrder(int presentationOrder) { this.presentationOrder = presentationOrder; }

    /** Returns the presentation order. */
    public int getPresentationOrder() { return presentationOrder; }

    /** Sets the poll. */
    public void setPoll(Poll poll) { this.poll = poll; }

    /** Returns the poll. */
    public Poll getPoll() { return poll; }

    /** Sets the set of votes. */
    public void setVotes(Set<Vote> votes) { this.votes = votes; }

    /** Returns the set of votes for this option. */
    public Set<Vote> getVotes() { return votes; }
}
