package com.DAT250Project.PollApp.messaging;

import com.DAT250Project.PollApp.model.Vote;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class VotePublisher {

    private final RabbitTemplate rabbitTemplate;

    public VotePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishVote(Vote vote) {
        // Use poll ID as routing key to create per-poll topic behavior
        String routingKey = vote.getOption().getPoll().getId().toString();
        rabbitTemplate.convertAndSend("pollExchange", "", vote); // fanout ignores routingKey
    }
}