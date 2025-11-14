package com.DAT250Project.PollApp.messaging;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "app.exchange";

    // routing keys
    public static final String USER_CREATED_KEY = "user.created";
    public static final String POLL_CREATED_KEY = "poll.created";
    public static final String VOTE_CREATED_KEY = "vote.created";

    // queue names (matching ConsoleConsumer)
    public static final String USER_CREATED_QUEUE = "user.created.queue";
    public static final String POLL_CREATED_QUEUE = "poll.created.queue";
    public static final String VOTE_CREATED_QUEUE = "vote.created.queue";

    @Bean
    public TopicExchange appExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue userCreatedQueue() {
        return new Queue(USER_CREATED_QUEUE, true);
    }

    @Bean
    public Queue pollCreatedQueue() {
        return new Queue(POLL_CREATED_QUEUE, true);
    }

    @Bean
    public Queue voteCreatedQueue() {
        return new Queue(VOTE_CREATED_QUEUE, true);
    }

    @Bean
    public Binding bindUserCreated() {
        return BindingBuilder
                .bind(userCreatedQueue())
                .to(appExchange())
                .with(USER_CREATED_KEY);
    }

    @Bean
    public Binding bindPollCreated() {
        return BindingBuilder
                .bind(pollCreatedQueue())
                .to(appExchange())
                .with(POLL_CREATED_KEY);
    }

    @Bean
    public Binding bindVoteCreated() {
        return BindingBuilder
                .bind(voteCreatedQueue())
                .to(appExchange())
                .with(VOTE_CREATED_KEY);
    }

    // ADDED - Use JSON instead of Java serialization
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}