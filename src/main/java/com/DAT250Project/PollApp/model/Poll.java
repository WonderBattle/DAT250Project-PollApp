package com.DAT250Project.PollApp.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "polls")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class Poll {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String question;

    private Instant publishedAt;

    @Column(nullable = false)
    private Instant validUntil;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    private User createdBy;

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("presentationOrder ASC")
    private List<VoteOption> options = new ArrayList<>();

    //CONSTRUCTORS
    public Poll (){

    }

    public Poll(String question, User createdBy) {
        this.question = question;
        this.createdBy = createdBy;
    }

    //CLARA
    public Poll(UUID id, String question, User createdBy,  Instant validUntil, List<VoteOption> options) {
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

    //SETTERS AND GETTERS
    public void setId(UUID id) {
        this.id = id;
    }
    public UUID getId() {
        return id;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
    public String getQuestion() {
        return question;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }
    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setValidUntil(Instant validUntil) {
        this.validUntil=validUntil;
    }
    public Instant getValidUntil() {
        return validUntil;
    }

    //Relationships setters and getters
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
    public User getCreatedBy() {
        return createdBy;
    }

    public List<VoteOption> getOptions() {
        return options;
    }
    public void setOptions(List<VoteOption> options) {
        this.options = options;
    }

    public VoteOption getOption(int i){
        return options.get(i);  // idk if is i or i-1
    }

    //METHODS
    //TODO choose between default constructor or parametrized constructor
    public VoteOption addVoteOption(String caption) {
        int order = options.size()+1;            // <-- Determine presentationOrder
        VoteOption option = new VoteOption(caption, order, this);
        this.options.add(option);
        return option;
    }

    /*
    public VoteOption addVoteOption(String caption) {
        VoteOption option = new VoteOption();
        option.setCaption(caption);
        option.setPresentationOrder(options.size());
        option.setPoll(this);
        this.options.add(option);
        return option;
    }
    */

}
