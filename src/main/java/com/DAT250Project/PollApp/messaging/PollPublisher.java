package com.DAT250Project.PollApp.messaging;

import com.DAT250Project.PollApp.model.Poll;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class PollPublisher {
    // this will log the message to the console in the frontend too
    private static final Logger logger = LoggerFactory.getLogger(PollPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public PollPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishPollCreated(Poll poll) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.POLL_CREATED_KEY,
                poll
        );
        logger.info("Sent PollCreated event: " + poll.getId());
    }
}