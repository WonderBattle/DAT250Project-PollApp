package com.DAT250Project.PollApp.messaging;

import com.DAT250Project.PollApp.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserPublisher {
    // this will log the message to the console in the frontend too
    private static final Logger logger = LoggerFactory.getLogger(UserPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public UserPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUserCreated(User user) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.USER_CREATED_KEY,
                user
        );
        logger.info("Sent UserCreated event: " + user.getId());
    }
}