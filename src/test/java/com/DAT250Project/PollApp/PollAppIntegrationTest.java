package com.DAT250Project.PollApp;

import com.DAT250Project.PollApp.model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full end-to-end test for PollApp covering:
 *  - User CRUD
 *  - Poll CRUD
 *  - Adding options
 *  - Casting and updating votes
 *  - Getting all votes, polls, users, and options
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PollAppIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private static UUID userId;
    private static UUID pollId;
    private static UUID option1Id;
    private static UUID option2Id;
    private static UUID voteId;

    // ============================================================
    // USER TESTS
    // ============================================================

    @Test
    @Order(1)
    void createUser() {
        User newUser = new User("alice", "alice@example.com");

        ResponseEntity<User> response = rest.postForEntity(url("/users"), newUser, User.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        userId = Objects.requireNonNull(response.getBody()).getId();
        assertThat(userId).isNotNull();
    }

    @Test
    @Order(2)
    void listAllUsers() {
        ResponseEntity<User[]> response = rest.getForEntity(url("/users"), User[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).length).isGreaterThan(0);
    }

    // ============================================================
    // POLL TESTS
    // ============================================================

    @Test
    @Order(3)
    void createPoll() {
        User creator = new User();
        creator.setId(userId);
        Poll poll = new Poll("What's your favorite language?", creator);

        ResponseEntity<Poll> response = rest.postForEntity(url("/polls"), poll, Poll.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        pollId = Objects.requireNonNull(response.getBody()).getId();
        assertThat(pollId).isNotNull();
    }

    @Test
    @Order(4)
    void listAllPolls() {
        ResponseEntity<Poll[]> response = rest.getForEntity(url("/polls"), Poll[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).length).isGreaterThan(0);
    }

    @Test
    @Order(5)
    void getPollById() {
        ResponseEntity<Poll> response = rest.getForEntity(url("/polls/" + pollId), Poll.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).getId()).isEqualTo(pollId);
    }


    // ============================================================
    // OPTION TESTS
    // ============================================================

    @Test
    @Order(6)
    void addOptionsToPoll() {
        VoteOption opt1 = new VoteOption();
        opt1.setCaption("Java");
        VoteOption opt2 = new VoteOption();
        opt2.setCaption("Python");

        ResponseEntity<VoteOption> r1 =
                rest.postForEntity(url("/polls/" + pollId + "/options"), opt1, VoteOption.class);
        ResponseEntity<VoteOption> r2 =
                rest.postForEntity(url("/polls/" + pollId + "/options"), opt2, VoteOption.class);

        assertThat(r1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        option1Id = Objects.requireNonNull(r1.getBody()).getId();
        option2Id = Objects.requireNonNull(r2.getBody()).getId();

        assertThat(option1Id).isNotNull();
        assertThat(option2Id).isNotNull();
    }

    @Test
    @Order(7)
    void getOptionsByPoll() {
        ResponseEntity<VoteOption[]> response =
                rest.getForEntity(url("/polls/" + pollId + "/options"), VoteOption[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<VoteOption> options = Arrays.asList(Objects.requireNonNull(response.getBody()));
        assertThat(options).hasSizeGreaterThanOrEqualTo(2);
    }

    // ============================================================
    // VOTE TESTS
    // ============================================================

    @Test
    @Order(8)
    void castVote() {
        Vote vote = new Vote();
        vote.setVoterId(userId);
        vote.setOptionId(option1Id);

        ResponseEntity<Vote> response =
                rest.postForEntity(url("/polls/" + pollId + "/votes"), vote, Vote.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        voteId = Objects.requireNonNull(response.getBody()).getId();
        assertThat(voteId).isNotNull();
        assertThat(response.getBody().getOption().getId()).isEqualTo(option1Id);
    }

    @Test
    @Order(9)
    void getVotesByUser() {
        ResponseEntity<Vote[]> response =
                rest.getForEntity(url("/users/" + userId + "/votes"), Vote[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<Vote> votes = Arrays.asList(Objects.requireNonNull(response.getBody()));
        assertThat(votes).hasSize(1);
        assertThat(votes.get(0).getOption().getId()).isEqualTo(option1Id);
    }

    @Test
    @Order(10)
    void updateVoteToAnotherOption() {
        Vote updated = new Vote();
        updated.setVoterId(userId);
        updated.setOptionId(option2Id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Vote> request = new HttpEntity<>(updated, headers);
        ResponseEntity<Vote> response =
                rest.exchange(url("/polls/" + pollId + "/votes"), HttpMethod.PUT, request, Vote.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).getOption().getId()).isEqualTo(option2Id);
    }

    @Test
    @Order(11)
    void getVotesByPoll() {
        ResponseEntity<Vote[]> response =
                rest.getForEntity(url("/polls/" + pollId + "/votes"), Vote[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<Vote> votes = Arrays.asList(Objects.requireNonNull(response.getBody()));
        assertThat(votes).hasSize(1);
        assertThat(votes.get(0).getOption().getId()).isEqualTo(option2Id);
    }

    // ============================================================
    // DELETE TESTS
    // ============================================================

    @Test
    @Order(12)
    void deletePoll() {
        rest.delete(url("/polls/" + pollId));

        ResponseEntity<Poll> response = rest.getForEntity(url("/polls/" + pollId), Poll.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(13)
    void deleteUser() {
        rest.delete(url("/users/" + userId));

        ResponseEntity<User> response = rest.getForEntity(url("/users/" + userId), User.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
