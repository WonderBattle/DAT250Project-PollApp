package com.DAT250Project.PollApp.messaging;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Use a fanout exchange so all subscribers get the message
    @Bean
    public FanoutExchange pollExchange() {
        return new FanoutExchange("pollExchange");
    }

    // later make unique for each poll if needed but I don't think so
    @Bean
    public Queue defaultQueue() {
        return new Queue("defaultPollQueue", true);
    }

    @Bean
    public Binding binding(Queue defaultQueue, FanoutExchange pollExchange) {
        return BindingBuilder.bind(defaultQueue).to(pollExchange);
    }
}