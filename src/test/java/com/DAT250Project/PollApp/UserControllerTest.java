package com.DAT250Project.PollApp;

import com.DAT250Project.PollApp.model.*;
import com.DAT250Project.PollApp.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserController.
 *
 * Each test is independent: data is created in @BeforeEach and rolled back after the test
 * thanks to @Transactional. Tests assert the REST endpoints using MockMvc (HTTP calls).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc; // used to perform HTTP requests to controllers

    @Autowired
    private ObjectMapper objectMapper; // convert objects to/from JSON

    // repositories used for test setup (direct DB access)
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PollRepository pollRepository;
    @Autowired
    private VoteOptionRepository voteOptionRepository;
    @Autowired
    private VoteRepository voteRepository;

    // seeded entities for each test
    private User alice;
    private Poll alicePoll;
    private VoteOption redOption;
    private Vote vote;

    @BeforeEach
    void setUp() {
        // clean DB (defensive; transactional will rollback anyway)
        voteRepository.deleteAll();
        voteOptionRepository.deleteAll();
        pollRepository.deleteAll();
        userRepository.deleteAll();

        // Create a user (Alice) and save to DB directly (setup only)
        alice = new User("alice", "alice@example.com");
        alice = userRepository.save(alice);

        // Create a poll for Alice and persist
        alicePoll = new Poll();
        alicePoll.setQuestion("What's your favorite colour?");
        alicePoll.setPublishedAt(Instant.now());
        alicePoll.setValidUntil(Instant.now().plusSeconds(3600 * 24)); // 1 day
        alicePoll.setCreatedBy(alice); // set bidirectional relation in PollManager normally
        alicePoll = pollRepository.save(alicePoll);

        // Create a vote option (red) for the poll and persist
        redOption = new VoteOption("Red", 1, alicePoll);
        redOption = voteOptionRepository.save(redOption);

        // Create a vote by Alice on the red option
        vote = new Vote(alice, redOption);
        vote = voteRepository.save(vote);
    }

    @Test
    @DisplayName("GET /users returns list containing Alice")
    void getAllUsers_returnsAlice() throws Exception {
        // call GET /users and expect list with at least one user
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                // expecting that at least one user exists and the first user's username is "alice" may vary,
                // so here we assert by searching the returned JSON for alice's username presence
                .andExpect(jsonPath("$[?(@.username=='alice')]").exists());
    }

    @Test
    @DisplayName("POST /users creates a new user")
    void createUser_createsUser() throws Exception {
        User bob = new User("bob", "bob@example.com");

        // perform POST /users with JSON body
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bob)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("bob"))
                .andReturn().getResponse().getContentAsString();

        // parse returned user and assert it was saved to DB
        User created = objectMapper.readValue(response, User.class);
        assertThat(userRepository.findById(created.getId())).isPresent();
    }

    @Test
    @DisplayName("GET /users/{userId} returns the user")
    void getUserById_returnsUser() throws Exception {
        mockMvc.perform(get("/users/{userId}", alice.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    @DisplayName("GET /users/{userId}/polls returns user's polls")
    void getUserPolls_returnsPolls() throws Exception {
        mockMvc.perform(get("/users/{userId}/polls", alice.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].question").value("What's your favorite colour?"));
    }

    @Test
    @DisplayName("GET /users/{userId}/votes returns user's votes")
    void getUserVotes_returnsVotes() throws Exception {
        mockMvc.perform(get("/users/{userId}/votes", alice.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].voter.id").value(alice.getId().toString()));
    }

    @Test
    @DisplayName("DELETE /users/{userId} deletes the user")
    void deleteUser_deletesUser() throws Exception {
        mockMvc.perform(delete("/users/{userId}", alice.getId()))
                .andExpect(status().isNoContent());

        // ensure user no longer exists in DB
        assertThat(userRepository.findById(alice.getId())).isEmpty();
    }
}