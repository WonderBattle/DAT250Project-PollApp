package com.DAT250Project.PollApp.messaging;

import com.DAT250Project.PollApp.model.Vote;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VotePublisher {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper; // Add this

    public VotePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

//    public void publishVote(Vote vote) {
//        // Use poll ID as routing key to create per-poll topic behavior
//        String routingKey = vote.getOption().getPoll().getId().toString();
//        rabbitTemplate.convertAndSend("pollExchange", "", vote); // fanout ignores routingKey
//    }

    public void publishVote(Vote vote) {
        try {
            String voteJson = objectMapper.writeValueAsString(vote);
            rabbitTemplate.convertAndSend("votes-exchange", "votes.routing.key", voteJson);
        } catch (Exception e) {
            System.err.println("Failed to publish vote to RabbitMQ: " + e.getMessage());
        }
    }
}