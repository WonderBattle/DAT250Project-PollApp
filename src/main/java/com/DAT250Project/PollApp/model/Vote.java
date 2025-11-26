package com.DAT250Project.PollApp.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

/**
 * Represents a vote cast by a user (or anonymous) on a poll option.
 */
@Entity
@Table(name = "votes")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Vote {

    /** Unique identifier for the vote. */
    @Id
    @GeneratedValue
    private UUID id;

    /** When the vote was submitted. */
    private Instant publishedAt;

    /** The user who cast the vote (nullable for anonymous). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id", nullable = true)
    @JsonIgnoreProperties({"votes", "createdPolls"})
    private User voter;

    /** The selected vote option. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    @JsonIgnoreProperties({"votes", "poll"})
    private VoteOption option;

    /** Default constructor. */
    public Vote() {}

    /**
     * Creates a new vote.
     *
     * @param voter the user casting the vote
     * @param option the option being voted for
     */
    public Vote(User voter, VoteOption option){
        this.voter = voter;
        this.option = option;
        this.publishedAt = Instant.now();
    }

    /** Sets the vote ID. */
    public void setId(UUID id) { this.id = id; }

    /** Returns the vote ID. */
    public UUID getId() { return id; }

    /** Sets when the vote was published. */
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }

    /** Returns when the vote was published. */
    public Instant getPublishedAt() { return publishedAt; }

    /** Sets the voter. */
    public void setVoter(User voter) { this.voter = voter; }

    /** Returns the voter. */
    public User getVoter() { return voter; }

    /** Returns the voter ID or null for anonymous. */
    public UUID getVoterId() {
        return voter != null ? voter.getId() : null;
    }

    /** Sets the voter ID safely. */
    public void setVoterId(UUID voterId){
        if (this.voter == null) this.voter = new User();
        this.voter.setId(voterId);
    }

    /** Sets the selected option. */
    public void setOption(VoteOption option) { this.option = option; }

    /** Returns the selected option. */
    public VoteOption getOption() { return option; }

    /** Returns the option ID. */
    public UUID getOptionId(){
        return option != null ? option.getId() : null;
    }

    /** Sets the option ID safely. */
    public void setOptionId(UUID optionId){
        if (this.option == null) this.option = new VoteOption();
        this.option.setId(optionId);
    }
}
