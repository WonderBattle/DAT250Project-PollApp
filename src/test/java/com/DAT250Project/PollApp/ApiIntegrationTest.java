package com.DAT250Project.PollApp;

import com.DAT250Project.PollApp.model.Poll;
import com.DAT250Project.PollApp.model.User;
import com.DAT250Project.PollApp.model.Vote;
import com.DAT250Project.PollApp.model.VoteOption;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PollApp REST API (User, Poll, Vote)
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static UUID userId;
    private static UUID pollId;
    private static UUID option1Id;
    private static UUID option2Id;
    private static UUID voteId;

    @Test
    @Order(1)
    void testCreateUser() throws Exception {
        User user = new User();
        user.setUsername("john_doe");

        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andReturn();

        User created = objectMapper.readValue(result.getResponse().getContentAsString(), User.class);
        userId = created.getId();
        assertThat(created.getUsername()).isEqualTo("john_doe");
    }

    @Test
    @Order(2)
    void testCreatePoll() throws Exception {
        Poll poll = new Poll();
        poll.setQuestion("Favorite Programming Language");

        // Create a poll with this user as creator
        User creator = new User();
        creator.setId(userId);
        poll.setCreatedBy(creator);

        MvcResult result = mockMvc.perform(post("/polls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(poll)))
                .andExpect(status().isCreated())
                .andReturn();

        Poll createdPoll = objectMapper.readValue(result.getResponse().getContentAsString(), Poll.class);
        pollId = createdPoll.getId();
        assertThat(createdPoll.getQuestion()).isEqualTo("Favorite Programming Language");
    }

    @Test
    @Order(3)
    void testAddOptionsToPoll() throws Exception {
        // Option 1
        VoteOption option1 = new VoteOption();
        option1.setCaption("Java");

        MvcResult res1 = mockMvc.perform(post("/polls/" + pollId + "/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(option1)))
                .andExpect(status().isCreated())
                .andReturn();
        option1Id = objectMapper.readValue(res1.getResponse().getContentAsString(), VoteOption.class).getId();

        // Option 2
        VoteOption option2 = new VoteOption();
        option2.setCaption("Python");

        MvcResult res2 = mockMvc.perform(post("/polls/" + pollId + "/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(option2)))
                .andExpect(status().isCreated())
                .andReturn();
        option2Id = objectMapper.readValue(res2.getResponse().getContentAsString(), VoteOption.class).getId();
    }

    @Test
    @Order(4)
    void testGetAllPollOptions() throws Exception {
        mockMvc.perform(get("/polls/" + pollId + "/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(5)
    void testCreateVote() throws Exception {
        Vote vote = new Vote();
        vote.setVoterId(userId);
        vote.setOptionId(option1Id);

        MvcResult result = mockMvc.perform(post("/polls/" + pollId + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vote)))
                .andExpect(status().isCreated())
                .andReturn();

        Vote createdVote = objectMapper.readValue(result.getResponse().getContentAsString(), Vote.class);
        voteId = createdVote.getId();
        assertThat(createdVote.getOption().getId()).isEqualTo(option1Id);
    }

    @Test
    @Order(6)
    void testUpdateVote() throws Exception {
        Vote vote = new Vote();
        vote.setVoterId(userId);
        vote.setOptionId(option2Id); // change to Python

        mockMvc.perform(put("/polls/" + pollId + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vote)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(7)
    void testGetVotesByPoll() throws Exception {
        mockMvc.perform(get("/polls/" + pollId + "/votes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(8)
    void testGetVotesByOption() throws Exception {
        mockMvc.perform(get("/" + option2Id + "/votes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(9)
    void testGetVoteById() throws Exception {
        mockMvc.perform(get("/votes/" + voteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(voteId.toString()));
    }

    @Test
    @Order(10)
    void testGetUserVotesAndPolls() throws Exception {
        mockMvc.perform(get("/users/" + userId + "/votes"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/" + userId + "/polls"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(11)
    void testDeleteVote() throws Exception {
        mockMvc.perform(delete("/votes/" + voteId))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(12)
    void testDeletePollAndUser() throws Exception {
        mockMvc.perform(delete("/polls/" + pollId))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/users/" + userId))
                .andExpect(status().isNoContent());
    }
}
