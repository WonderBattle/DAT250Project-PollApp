package com.DAT250Project.PollApp.repository;

import com.DAT250Project.PollApp.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, UUID> {
    // Find all votes by a specific user
    List<Vote> findByVoter_Id(UUID voterId);

    // Find all votes for a specific option
    List<Vote> findByOption_Id(UUID optionId);

    // Find vote by user and option (useful for checking if user already voted)
    Optional<Vote> findByVoter_IdAndOption_Id(UUID voterId, UUID optionId);

    // Find votes by user in a specific poll (via option's poll)
    List<Vote> findByVoter_IdAndOptionPoll_Id(UUID voterId, UUID pollId);

    // Check if user has voted in a specific poll
    boolean existsByVoter_IdAndOptionPoll_Id(UUID voterId, UUID pollId);
}