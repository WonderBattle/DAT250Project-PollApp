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

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Map;

/**
 * Integration tests for VoteController.
 *
 * Tests cover creating/updating/deleting votes and querying votes by option/poll/user.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VoteControllerTest {

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
    @Autowired
    private VoteRepository voteRepository;

    private User alice;
    private Poll poll;
    private VoteOption red;
    private VoteOption blue;
    private Vote aliceVote;

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        // cleanup
        voteRepository.deleteAll();
        voteOptionRepository.deleteAll();
        pollRepository.deleteAll();
        userRepository.deleteAll();

        // seed user
        alice = new User("alice", "alice@example.com");
        alice.setPassword(new BCryptPasswordEncoder().encode("password123"));
        alice = userRepository.save(alice);

        // seed poll
        poll = new Poll();
        poll.setQuestion("Pick one");
        poll.setPublishedAt(Instant.now());
        poll.setValidUntil(Instant.now().plusSeconds(3600 * 24));
        poll.setCreatedBy(alice);
        poll = pollRepository.save(poll);

        // seed options
        red = voteOptionRepository.save(new VoteOption("Red", 1, poll));
        blue = voteOptionRepository.save(new VoteOption("Blue", 2, poll));

        // seed vote (Alice votes Red)
        aliceVote = voteRepository.save(new Vote(alice, red));

        // Obtain JWT token for alice
        jwtToken = obtainAccessToken(alice.getEmail());
    }


    @Test
    @DisplayName("POST /polls/{pollId}/votes creates a vote")
    void createVote_createsVote() throws Exception {
        Vote request = new Vote();
        request.setVoterId(alice.getId());
        request.setOptionId(blue.getId());

        mockMvc.perform(post("/polls/{pollId}/votes", poll.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.option").value(blue.getId().toString())) // Changed this line
                .andExpect(jsonPath("$.voter.id").value(alice.getId().toString()));
    }

    @Test
    @DisplayName("GET /votes returns all votes")
    void getAllVotes_returnsVotes() throws Exception {
        mockMvc.perform(get("/votes")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /{optionId}/votes returns votes for option")
    void getVotesByOption_returnsVotes() throws Exception {
        mockMvc.perform(get("/{optionId}/votes", red.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /polls/{pollId}/votes returns votes for poll")
    void getVotesByPoll_returnsVotes() throws Exception {
        mockMvc.perform(get("/polls/{pollId}/votes", poll.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /votes/{voteId} returns a vote")
    void getVoteById_returnsVote() throws Exception {
        mockMvc.perform(get("/votes/{voteId}", aliceVote.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(aliceVote.getId().toString()));
    }

    @Test
    @DisplayName("PUT /polls/{pollId}/votes updates a vote")
    void updateVote_updatesVote() throws Exception {
        Vote updateReq = new Vote();
        updateReq.setVoterId(alice.getId());
        updateReq.setOptionId(blue.getId());

        mockMvc.perform(put("/polls/{pollId}/votes", poll.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.option").value(blue.getId().toString())); // Changed this line
    }

    @Test
    @DisplayName("DELETE /votes/{voteId} deletes a vote")
    void deleteVote_deletesVote() throws Exception {
        mockMvc.perform(delete("/votes/{voteId}", aliceVote.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        assertThat(voteRepository.findById(aliceVote.getId())).isEmpty();
    }

    @Test
    @DisplayName("POST /votes allows anonymous votes without authentication")
    void createVote_anonymousUserCanVote() throws Exception {
        // Get any option from the seeded poll
        VoteOption option = voteOptionRepository.findAll().get(0);

        // Create vote request with null voter (anonymous)
        Map<String, Object> voteRequest = Map.of(
                "optionId", option.getId().toString()
                // No voterId = anonymous vote
        );

        mockMvc.perform(post("/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voteRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.optionId").value(option.getId().toString()))
                .andExpect(jsonPath("$.voterId").doesNotExist()); // Should be null for anonymous
    }

    private String obtainAccessToken(String email) throws Exception {
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