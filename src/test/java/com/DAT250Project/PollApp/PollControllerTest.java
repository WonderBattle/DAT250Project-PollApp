package com.DAT250Project.PollApp;

import com.DAT250Project.PollApp.model.Poll;
import com.DAT250Project.PollApp.model.User;
import com.DAT250Project.PollApp.model.VoteOption;
import com.DAT250Project.PollApp.repository.PollRepository;
import com.DAT250Project.PollApp.repository.UserRepository;
import com.DAT250Project.PollApp.repository.VoteOptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Map;


/**
 * Integration tests for PollController (including options endpoints).
 *
 * Each test seeds required data and then uses MockMvc to call the controller endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PollRepository pollRepository;
    @Autowired
    private VoteOptionRepository voteOptionRepository;

    private User alice;
    private Poll poll;

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {

        // Clean DB
        voteOptionRepository.deleteAll();
        pollRepository.deleteAll();
        userRepository.deleteAll();

        // seed user (Alice) with a password so we can log in via /auth/login in tests
        alice = new User("alice", "alice@example.com");
        // encode password for user (so login works)
        alice.setPassword(new BCryptPasswordEncoder().encode("password123"));
        alice = userRepository.save(alice);

        // seed poll
        poll = new Poll();
        poll.setQuestion("Which is best?");
        poll.setCreatedBy(alice);
        poll.setPublishedAt(Instant.now());
        poll.setValidUntil(Instant.now().plusSeconds(3600 * 24));
        poll = pollRepository.save(poll);

        // add two options directly via repository (PollController tests add option as well)
        VoteOption red = new VoteOption("Red", 1, poll);
        VoteOption green = new VoteOption("Green", 2, poll);
        voteOptionRepository.saveAll(List.of(red, green));

        // Obtain JWT token for alice (used for protected endpoints in tests)
        jwtToken = obtainAccessToken(alice.getEmail());
    }

    @Test
    @DisplayName("POST /polls creates poll")
    void createPoll_createsPoll() throws Exception {
        // Build a poll object referencing existing user by id (this mimics API usage)
        Poll request = new Poll();
        request.setQuestion("New poll?");
        request.setValidUntil(Instant.now().plusSeconds(3600 * 24));
        // we set createdBy with a minimal User having id so the controller will link it
        User creator = new User();
        creator.setId(alice.getId());
        request.setCreatedBy(creator);

        mockMvc.perform(post("/polls")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.question").value("New poll?"))
                .andExpect(jsonPath("$.createdBy.id").exists());
    }

    @Test
    @DisplayName("GET /polls returns polls")
    void getAllPolls_returnsPolls() throws Exception {
        mockMvc.perform(get("/polls")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.question=='Which is best?')]").exists());
    }

    @Test
    @DisplayName("GET /polls/{id} returns poll")
    void getPollById_returnsPoll() throws Exception {
        mockMvc.perform(get("/polls/{pollId}", poll.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value("Which is best?"));
    }

    @Test
    @DisplayName("POST /polls/{pollId}/options adds an option")
    void addOption_addsOption() throws Exception {
        VoteOption blue = new VoteOption();
        blue.setCaption("Blue");
        blue.setPresentationOrder(3);

        var mvcResult = mockMvc.perform(post("/polls/{pollId}/options", poll.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blue)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.caption").value("Blue"))
                .andReturn();

        // verify DB contains new option
        VoteOption created = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), VoteOption.class);
        assertThat(voteOptionRepository.findById(created.getId())).isPresent();
    }

    @Test
    @DisplayName("GET /polls/{pollId}/options returns all options")
    void getAllOptions_returnsOptions() throws Exception {
        mockMvc.perform(get("/polls/{pollId}/options", poll.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)); // red + green seeded in setup
    }

    @Test
    @DisplayName("DELETE /polls/{pollId}/options/{optionId} removes option")
    void deleteOption_deletesOption() throws Exception {
        // fetch one option to delete
        var options = voteOptionRepository.findAll();
        VoteOption toDelete = options.get(0);

        mockMvc.perform(delete("/polls/{pollId}/options/{optionId}", poll.getId(), toDelete.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        assertThat(voteOptionRepository.findById(toDelete.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /polls/{pollId} deletes poll")
    void deletePoll_deletesPoll() throws Exception {
        mockMvc.perform(delete("/polls/{pollId}", poll.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        assertThat(pollRepository.findById(poll.getId())).isEmpty();
    }

    @Test
    @DisplayName("GET /polls/public returns public polls without auth")
    void getPublicPolls_returnsPublicPollsWithoutAuth() throws Exception {
        // Make poll public
        poll.setPublicPoll(true);
        pollRepository.save(poll);

        mockMvc.perform(get("/polls/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.question=='Which is best?')]").exists());
    }

    @Test
    @DisplayName("GET /polls/private/{userId} returns user's private polls")
    void getPrivatePolls_returnsPrivatePolls() throws Exception {
        // Make poll private
        poll.setPublicPoll(false);
        pollRepository.save(poll);

        mockMvc.perform(get("/polls/private/{userId}", alice.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.question=='Which is best?')]").exists());
    }


    private String obtainAccessToken(String email) throws Exception {
        // Build login payload
        Map<String, String> loginPayload = Map.of(
                "email", email,
                "password", "password123"
        );

        var mvcResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andReturn();

        var json = mvcResult.getResponse().getContentAsString();
        var node = objectMapper.readTree(json);
        return node.get("token").asText();
    }
}
