// VoteOptionRepository.java
package com.DAT250Project.PollApp.repository;

import com.DAT250Project.PollApp.model.VoteOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoteOptionRepository extends JpaRepository<VoteOption, UUID> {
    // Find all options for a specific poll
    List<VoteOption> findByPollId(UUID pollId);

    // Find options by poll ID ordered by presentation order
    List<VoteOption> findByPollIdOrderByPresentationOrderAsc(UUID pollId);
}