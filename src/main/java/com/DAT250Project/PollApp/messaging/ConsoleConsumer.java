package com.DAT250Project.PollApp.messaging;

import com.DAT250Project.PollApp.model.Poll;
import com.DAT250Project.PollApp.model.User;
import com.DAT250Project.PollApp.model.Vote;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

//another log printing method to the console, but I think the publisher classes are enough so this is just optional experimenting
@Component
public class ConsoleConsumer {

    @RabbitListener(queues = RabbitMQConfig.USER_CREATED_QUEUE)
    public void handleUserCreated(User user) {
        System.out.println("(Console) User created event → " + user.getEmail());
    }

    @RabbitListener(queues = RabbitMQConfig.POLL_CREATED_QUEUE)
    public void handlePollCreated(Poll poll) {
        System.out.println("(Console) Poll created event → " + poll.getQuestion());
    }

    @RabbitListener(queues = RabbitMQConfig.VOTE_CREATED_QUEUE)
    public void handleVoteCreated(Vote vote) {
        System.out.println("(Console) Vote created event → VoteID=" + vote.getId());
    }
}