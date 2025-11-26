package com.DAT250Project.PollApp.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

/**
 * Represents a poll containing a question, visibility settings, creator, and vote options.
 */
@Entity
@Table(name = "polls")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Poll {

    /** Unique identifier of the poll. */
    @Id
    @GeneratedValue
    private UUID id;

    /** The question being asked in the poll. */
    @Column(nullable = false)
    private String question;

    /** Timestamp for when the poll was published. */
    private Instant publishedAt;

    /** Timestamp indicating until when the poll is valid. */
    @Column(nullable = false)
    private Instant validUntil;

    /** Indicates whether the poll is public or private. */
    @Column(nullable = false)
    private boolean publicPoll = true;

    /** User who created the poll. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnoreProperties({"createdPolls", "votes"})
    private User createdBy;

    /** List of vote options associated with the poll. */
    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("presentationOrder ASC")
    @JsonIgnoreProperties("poll")
    private List<VoteOption> options = new ArrayList<>();

    /** Default constructor. */
    public Poll() {}

    /**
     * Constructor for a poll with question and creator.
     */
    public Poll(String question, User createdBy) {
        this.question = question;
        this.createdBy = createdBy;
    }

    /**
     * Full constructor including options, creator, and validity.
     */
    public Poll(UUID id, String question, User createdBy, Instant validUntil, List<VoteOption> options) {
        this.id = id;
        this.question = question;
        if(createdBy != null) {
            this.createdBy = createdBy;
            createdBy.getCreatedPolls().add(this);
        }
        this.publishedAt = Instant.now();
        this.validUntil = validUntil;
        int i = 1;
        for(VoteOption option : options) {
            option.setPresentationOrder(i);
            this.options.add(option);
            i++;
        }
    }

    /** Sets the poll ID. */
    public void setId(UUID id) { this.id = id; }

    /** Returns the poll ID. */
    public UUID getId() { return id; }

    /** Sets the poll question. */
    public void setQuestion(String question) { this.question = question; }

    /** Returns the poll question. */
    public String getQuestion() { return question; }

    /** Sets the publish timestamp. */
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }

    /** Returns when the poll was published. */
    public Instant getPublishedAt() { return publishedAt; }

    /** Sets validity timestamp. */
    public void setValidUntil(Instant validUntil) { this.validUntil = validUntil; }

    /** Returns validity timestamp. */
    public Instant getValidUntil() { return validUntil; }

    /** Returns whether poll is public. */
    public boolean isPublicPoll() { return publicPoll; }

    /** Sets the poll visibility. */
    public void setPublicPoll(boolean publicPoll) { this.publicPoll = publicPoll; }

    /** Sets the poll creator. */
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    /** Returns the poll creator. */
    public User getCreatedBy() { return createdBy; }

    /** Returns the list of vote options. */
    public List<VoteOption> getOptions() { return options; }

    /** Sets the vote options list. */
    public void setOptions(List<VoteOption> options) { this.options = options; }

    /**
     * Retrieves a specific option by index.
     */
    public VoteOption getOption(int i) { return options.get(i); }

    /**
     * Adds a new vote option to the poll.
     *
     * @param caption text of the vote option
     * @return the created VoteOption
     */
    public VoteOption addVoteOption(String caption) {
        int order = options.size() + 1;
        VoteOption option = new VoteOption(caption, order, this);
        this.options.add(option);
        return option;
    }
}
