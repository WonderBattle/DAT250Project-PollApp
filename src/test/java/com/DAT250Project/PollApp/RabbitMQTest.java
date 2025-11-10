package com.DAT250Project.PollApp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RabbitMQTest {

    private static CachingConnectionFactory connectionFactory;
    private static RabbitTemplate rabbitTemplate;
    private static BlockingQueue<String> blockingQueue;
    private static ObjectMapper objectMapper;
    private static final String QUEUE_NAME = "votes";

    @BeforeAll
    static void setup() {
        connectionFactory = new CachingConnectionFactory("localhost", 5672);
        rabbitTemplate = new RabbitTemplate(connectionFactory);
        blockingQueue = new ArrayBlockingQueue<>(1);
        objectMapper = new ObjectMapper();

        // 1. Explicitly declare the queue in RabbitMQ
        rabbitTemplate.execute(channel -> {
            channel.queueDeclare("votes", false, false, false, null);
            return null;
        });

        // 2. Listener container
        Queue queue = new Queue(QUEUE_NAME, false);
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueues(queue);
        container.setMessageListener((message) -> {
            try {
                blockingQueue.put(new String(message.getBody()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        container.start();
    }

    @AfterAll
    static void tearDown() {
        connectionFactory.destroy();
    }

    @Test
    void testPublishAndConsumeVote() throws Exception {

        Map<String, Object> voteMessage = Map.of(
                "pollId", "1234",
                "userId", "user-42",
                "optionId", "opt-A"
        );

        // Send vote JSON
        String json = objectMapper.writeValueAsString(voteMessage);
        rabbitTemplate.convertAndSend(QUEUE_NAME, json);

        // Receive through listener
        String receivedJson = blockingQueue.take();
        Map<?, ?> received = objectMapper.readValue(receivedJson, Map.class);

        assertEquals(voteMessage, received);

        System.out.println("✅ Received vote message: " + received);
    }
}