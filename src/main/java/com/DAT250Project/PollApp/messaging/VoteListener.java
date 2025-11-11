package com.DAT250Project.PollApp.messaging;

import com.DAT250Project.PollApp.model.Vote;
import com.DAT250Project.PollApp.repository.VoteRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class VoteListener {

    private final VoteRepository voteRepository;

    public VoteListener(VoteRepository voteRepository) {
        this.voteRepository = voteRepository;
    }

    @RabbitListener(queues = "defaultPollQueue")
    public void handleVote(Vote vote) {
        // This is triggered whenever a vote is published
        System.out.println("Received vote for option " + vote.getOption().getId() + " by user " + vote.getVoter().getId());

        // Optionally persist/update vote in DB if coming from external sources
        voteRepository.save(vote);
    }
}