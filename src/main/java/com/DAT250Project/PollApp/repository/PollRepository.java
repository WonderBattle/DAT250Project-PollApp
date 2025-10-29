package com.DAT250Project.PollApp.repository;

import com.DAT250Project.PollApp.model.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface PollRepository extends JpaRepository<Poll, UUID> {
    // Find polls by creator
    List<Poll> findByCreatedById(UUID userId);

    // Find active polls (not expired)
    List<Poll> findByValidUntilAfter(java.time.Instant currentTime);

    // Find expired polls
    List<Poll> findByValidUntilBefore(java.time.Instant currentTime);
}