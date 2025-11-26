package com.DAT250Project.PollApp;

import com.DAT250Project.PollApp.CacheConfig.RedisCacheService;
import com.DAT250Project.PollApp.messaging.PollPublisher;
import com.DAT250Project.PollApp.messaging.UserPublisher;
import com.DAT250Project.PollApp.messaging.VotePublisher;
import com.DAT250Project.PollApp.model.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;


import com.DAT250Project.PollApp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service responsible for managing users, polls, options, and votes.
 * <p>
 * This class serves as a central manager for all core business logic in the PollApp system.
 * It handles entity creation, updates, deletion, caching rules, and event publication.
 * </p>
 */
@Service
public class PollManager {

    // === JPA Repositories ===

    /** Repository for user persistence. */
    @Autowired
    private UserRepository userRepository;

    /** Repository for poll persistence. */
    @Autowired
    private PollRepository pollRepository;

    /** Repository for vote option persistence. */
    @Autowired
    private VoteOptionRepository voteOptionRepository;

    /** Repository for vote persistence. */
    @Autowired
    private VoteRepository voteRepository;

    /** Message publisher for user events. */
    @Autowired
    private UserPublisher userPublisher;

    /** Message publisher for poll events. */
    @Autowired
    private PollPublisher pollPublisher;

    /** Message publisher for vote events. */
    @Autowired
    private VotePublisher votePublisher;

    /** Redis caching service for polls, options, users, and votes. */
    @Autowired
    private RedisCacheService redisCacheService;

    /** Password encoder for hashing user passwords. */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /** Default constructor (Spring handles dependency injection). */
    public PollManager() {}

    //------------------------------------------------ USER ------------------------------------------------------------

    /**
     * Creates a new user, hashes the password if provided,
     * invalidates cache, persists the user, and publishes a creation message.
     *
     * @param user The user to create.
     * @return The saved user.
     */
    public User createUser(User user) {
        // If password provided, hash it before saving
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Invalidate users cache after creation
        redisCacheService.delete("all_users", null);
        User saved = userRepository.save(user);

        userPublisher.publishUserCreated(saved);

        return saved;
    }

    /**
     * Returns all users using a cache-first strategy.
     *
     * @return List of all users.
     */
    public List<User> getAllUsers() {
        // Try to get from cache first
        Object cachedUsers = redisCacheService.getAllUsers();
        if (cachedUsers instanceof List) {
            return (List<User>) cachedUsers;
        }

        // If not in cache, get from database and cache it
        List<User> users = userRepository.findAll();
        redisCacheService.cacheAllUsers(users);
        return users;
    }

    /**
     * Retrieves a user by ID using a cache-first lookup.
     *
     * @param userId The ID of the user.
     * @return The user or null if not found.
     */
    public User getUserById(UUID userId) {
        // Try to get from cache first using type-safe method
        User cachedUser = redisCacheService.get("user", userId, User.class);
        // If found in cache, return immediately (cache hit)
        if (cachedUser != null) {
            return cachedUser;
        }

        // If not in cache, search in database (cache miss)
        User user = userRepository.findById(userId).orElse(null);
        // If user found in database
        if (user != null) {
            // Save to cache for future queries (cache population)
            redisCacheService.cacheUser(userId, user);
        }
        return user;
    }

    /**
     * Deletes a user by ID, cleans related caches, and returns the deleted entity.
     *
     * @param userId The ID of the user to delete.
     * @return The deleted user or null if not found.
     */
    public User deleteUserById(UUID userId) {
        // Find user first to return it, then delete from database
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            userRepository.deleteById(userId);
            // Invalidate all related caches
            redisCacheService.delete("user", userId);
            redisCacheService.delete("all_users", null);
            redisCacheService.delete("user_polls", userId);
        }
        return user;
    }

    /**
     * Returns all polls created by a specific user using a cache-first strategy.
     *
     * @param userId The user ID.
     * @return List of polls or null if user does not exist.
     */
    public List<Poll> getPollsByUser(UUID userId) {
        // Try cache first
        Object cachedPolls = redisCacheService.getUserPolls(userId);
        if (cachedPolls instanceof List) {
            return (List<Poll>) cachedPolls;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        List<Poll> polls = new ArrayList<>(user.getCreatedPolls());
        // Cache the result
        redisCacheService.cacheUserPolls(userId, polls);
        // Convert the Set<Poll> to List<Poll> because controller methods return a List<>, not a Set<>
        return new ArrayList<>(user.getCreatedPolls());
    }

    /**
     * Retrieves all votes made by a specific user.
     *
     * @param userId The user's ID.
     * @return List of votes or null if user not found.
     */
    public List<Vote> getVotesByUser(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;
        // Convert the Set<Poll> to List<Poll> because controller methods return a List<>, not a Set<>
        return new ArrayList<>(user.getVotes());
    }

    //------------------------------------------------- POLL -----------------------------------------------------------

    /**
     * Creates a new poll, assigns timestamps, handles creator linking,
     * assigns option ordering, saves to DB, publishes creation event,
     * and invalidates cache.
     *
     * @param poll The poll to create.
     * @return The saved poll or null if creator invalid.
     */
    public Poll createPoll(Poll poll) {
        // No need to assign ID manually - JPA will handle it with @GeneratedValue

        // Assign validUntil and publishedAt
        poll.setPublishedAt(Instant.now());
        // if no validUntil is set by user, set it to +7 days
        if (poll.getValidUntil() == null) {
            poll.setValidUntil(Instant.now().plus(7, ChronoUnit.DAYS));
        }

        // Register the poll creator (if exists)
        User creator = poll.getCreatedBy();
        if (creator != null) {
            // If user exists in the database, use the managed entity
            User existingUser = userRepository.findById(creator.getId()).orElse(null);
            if (existingUser == null) {
                //TODO respone not user defined
                return null;
                // Save new user if doesn't exist
                //existingUser = userRepository.save(creator);
            }

            // Ensure bidirectional link uses the managed entity
            poll.setCreatedBy(existingUser);
            existingUser.getCreatedPolls().add(poll);
        } else {
            //TODO respone not user defined
            return null;
        }

        if (poll.getOptions() != null) {
            int order = 1;
            for (VoteOption option : poll.getOptions()) {
                option.setPresentationOrder(order++);
                option.setPoll(poll);  // ⭐ Set poll reference BEFORE saving poll
            }
        }

        Poll savedPoll = pollRepository.save(poll);
        //debug check, and I think security blocks rabbit right now, probably this is why we do not see the messages in the console
        System.out.println("Creating poll...");
        pollPublisher.publishPollCreated(savedPoll);

        // Invalidate relevant cache
        redisCacheService.delete("all_polls", null); // Clean complete list
        redisCacheService.delete("user_polls", savedPoll.getCreatedBy().getId());

        // Return the new poll
        return savedPoll;
    }

    /**
     * Retrieves all polls using a cache-first strategy.
     *
     * @return List of polls.
     */
    public List<Poll> getAllPolls() {
        // Get all polls from cache (uses simple key "all_polls")
        Object cached = redisCacheService.getAllPolls();
        // Check if cached object is a List and return it
        if (cached instanceof List) {
            return (List<Poll>) cached;
        }

        // If not in cache, get from database
        List<Poll> polls = pollRepository.findAll();
        // Cache the complete list
        redisCacheService.cacheAllPolls(polls);
        return polls;
    }

    /**
     * Returns all public polls.
     *
     * @return List of public polls.
     */
    public List<Poll> getPublicPolls() {
        return pollRepository.findByPublicPollTrue();
    }

    /**
     * Returns all private polls for a given user.
     *
     * @param userId The user ID.
     * @return List of private polls.
     */
    public List<Poll> getPrivatePolls(UUID userId) {
        return pollRepository.findByPublicPollFalseAndCreatedBy_Id(userId);
    }

    /**
     * Retrieves a poll by ID using a cache-first strategy.
     *
     * @param pollId The poll ID.
     * @return The poll or null.
     */
    public Poll getPollById(UUID pollId) {
        // Try to get poll from cache
        Poll cachedPoll = redisCacheService.get("poll", pollId, Poll.class);
        // Return cached poll if found
        if (cachedPoll != null) {
            return cachedPoll;
        }

        // If not in cache, query database
        Poll poll = pollRepository.findById(pollId).orElse(null);
        // If poll found, cache it
        if (poll != null) {
            redisCacheService.cachePoll(pollId, poll);
        }
        return poll;
    }

    /**
     * Deletes a poll by ID and clears related caches.
     *
     * @param pollId The poll ID.
     * @return The deleted poll or null.
     */
    public Poll deletePollById(UUID pollId) {

        // Find the poll first to return it
        Poll poll = pollRepository.findById(pollId).orElse(null);

        // If found, delete from database - cascading will handle related options
        // todo revise cascade problems
        if (poll != null) {
            // Get creator ID before deletion for cache invalidation
            UUID creatorId = poll.getCreatedBy().getId();

            // Invalidate all related caches
            redisCacheService.delete("poll", pollId);
            redisCacheService.delete("all_polls", null);
            redisCacheService.delete("user_polls", creatorId);
            redisCacheService.delete("poll_results", pollId);
            redisCacheService.delete("poll_votes", pollId);

            pollRepository.deleteById(pollId);
        }

        return poll;
    }

    /**
     * Updates the privacy (public/private) of a poll.
     *
     * @param pollId  The poll ID.
     * @param isPublic Whether the poll should be public.
     * @param userId The requesting user's ID.
     * @return Updated poll or null.
     */
    public Poll updatePollPrivacy(UUID pollId, boolean isPublic, UUID userId) {
        // Find the poll
        Poll poll = pollRepository.findById(pollId).orElse(null);
        if (poll == null) return null;

        // Check if user exists and is the poll owner (like your vote authorization)
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        if (!poll.getCreatedBy().getId().equals(userId)) {
            return null; // Not the owner - return null like your vote logic
        }

        // Update the privacy status
        poll.setPublicPoll(isPublic);

        // Save the updated poll
        Poll updatedPoll = pollRepository.save(poll);

        // Invalidate relevant caches
        redisCacheService.delete("poll", pollId);
        redisCacheService.delete("all_polls", null);
        redisCacheService.delete("user_polls", poll.getCreatedBy().getId());
        redisCacheService.delete("poll_results", pollId);

        return updatedPoll;
    }

    /**
     * Adds a new option to a poll and invalidates caches.
     *
     * @param pollId The poll ID.
     * @param option The option to add.
     * @return The saved option or null.
     */
    public VoteOption addOptionToPoll(UUID pollId, VoteOption option) {
        // Find the target poll from database
        Poll poll = pollRepository.findById(pollId).orElse(null);
        if (poll == null) return null;

        // No need to generate ID manually - JPA will handle it
        // Link it with the poll
        option.setPoll(poll);

        // Add it to the poll's option list
        poll.getOptions().add(option);

        // Save to database
        VoteOption savedOption = voteOptionRepository.save(option);

        // Update the poll to maintain consistency
        pollRepository.save(poll);

        // Invalidate caches since poll structure changed
        redisCacheService.delete("poll", pollId);
        redisCacheService.delete("poll_results", pollId);

        return savedOption;
    }

    /**
     * Deletes an option by ID and clears related caches.
     *
     * @param optionId The option ID.
     * @return Deleted option or null.
     */
    public VoteOption deleteOptionById(UUID optionId){
        VoteOption voteOption = voteOptionRepository.findById(optionId).orElse(null);

        if (voteOption != null){
            UUID pollId = voteOption.getPoll().getId();
            voteOptionRepository.deleteById(optionId);

            redisCacheService.delete("poll", pollId);
            redisCacheService.delete("poll_results", pollId);
            redisCacheService.delete("option", optionId);
        }
        return voteOption;
    }

    /**
     * Returns all options of a specific poll.
     *
     * @param pollId The poll ID.
     * @return List of options or null.
     */
    public List<VoteOption> getAllOptionsByPoll(UUID pollId) {
        /*  Before DB

         */
        Poll poll = pollRepository.findById(pollId).orElse(null);
        if (poll == null) return null;
        // Return the list of options for this poll
        return poll.getOptions();
    }

    /**
     * Retrieves a vote option by ID using cache-first strategy.
     *
     * @param optionId The option ID.
     * @return The option or null.
     */
    public VoteOption getOptionById(UUID optionId) {
        /*  Before DB

         */
        VoteOption cachedOption = redisCacheService.get("option", optionId, VoteOption.class);
        if (cachedOption != null) {
            return cachedOption;
        }

        VoteOption voteOption = voteOptionRepository.findById(optionId).orElse(null);
        if (voteOption != null) {
            redisCacheService.cacheVoteOption(optionId, voteOption);
        }
        return voteOption;
    }

    //----------------------------------------------- VOTE -------------------------------------------------------------

    /**
     * Checks whether a vote option belongs to a specific poll.
     *
     * @param optionId The option ID.
     * @param pollId The poll ID.
     * @return True if option belongs to poll, else false.
     */
    public boolean optionBelongsToPoll(UUID optionId, UUID pollId) {
        /*  Before DB

         */
        VoteOption option = voteOptionRepository.findById(optionId).orElse(null);
        if (option == null) return false;
        Poll poll = pollRepository.findById(pollId).orElse(null);
        if (poll == null) return false;

        // Check if this option belongs to the given poll
        return poll.getOptions().contains(option);
    }

    /**
     * Creates a vote for a poll, validates inputs, updates relationships,
     * saves the vote, publishes an event, and clears caches.
     *
     * @param pollId The poll ID.
     * @param voterId The voter ID or null for anonymous.
     * @param optionId The chosen option ID.
     * @return The saved vote or null if invalid.
     */
    public Vote createVote(UUID pollId, UUID voterId, UUID optionId) {
        /*  Before DB

         */
        // Validate existence of poll, user and option from database
        Poll poll = pollRepository.findById(pollId).orElse(null);
        VoteOption option = voteOptionRepository.findById(optionId).orElse(null);

        if (poll == null || option == null) {
            return null; // missing poll/option -> fail
        }

        User voter = null;
        if (voterId != null) {
            voter = userRepository.findById(voterId).orElse(null);
            if (voter == null) {
                return null; // invalid voter id provided
            }
        }

        //user is already voted in this poll
        if (voter != null && voteRepository.existsByVoter_IdAndOptionPoll_Id(voterId, pollId)) {
            return null;
        }

        // Ensure the option actually belongs to the poll
        if (option.getPoll() == null || !pollId.equals(option.getPoll().getId())) {
            return null; // invalid relationship - controller should treat as bad request
        }

        // Create Vote instance using your existing Vote constructors
        Vote vote = new Vote();                  // uses default constructor
        // No need to assign ID manually - JPA will handle it
        vote.setPublishedAt(Instant.now());      // record timestamp

        // Link vote to voter and option
        if (voter != null) {
            vote.setVoter(voter);
        } else {
            vote.setVoter(null); // anonymous vote
        }
        vote.setOption(option);

        // Persist in database
        Vote savedVote = voteRepository.save(vote);

        // if voter present, add to their votes
        if (voter != null) {
            voter.getVotes().add(savedVote);
            userRepository.save(voter);
        }

        // add the vote to the option's vote set
        option.getVotes().add(savedVote);
        voteOptionRepository.save(option);

        votePublisher.publishVote(savedVote);

        // Invalidate affected caches
        redisCacheService.delete("poll_results", pollId);
        redisCacheService.delete("poll_votes", pollId);
        redisCacheService.delete("poll", pollId);

        return savedVote;
    }

    /**
     * Updates an existing vote by changing the selected option.
     *
     * @param pollId The poll ID.
     * @param voterId The voter ID.
     * @param newOptionId The new option ID.
     * @return Updated vote or null on invalid request.
     */
    public Vote updateVote(UUID pollId, UUID voterId, UUID newOptionId) {
        /*  Before DB

         */
        Poll poll = pollRepository.findById(pollId).orElse(null);
        User voter = userRepository.findById(voterId).orElse(null);
        VoteOption newOption = voteOptionRepository.findById(newOptionId).orElse(null);

        // Basic existence checks
        if (poll == null || voter == null || newOption == null) return null;

        // Ensure new option belongs to the poll
        if (newOption.getPoll() == null || !pollId.equals(newOption.getPoll().getId())) {
            return null; // new option not in the same poll
        }

        // Find the voter's existing vote that is for this poll.
        // We search the user's votes and check if the vote's option belongs to this poll.
        Vote existingVote = null;
        for (Vote v : voter.getVotes()) {
            VoteOption currentOption = v.getOption();
            if (currentOption != null && currentOption.getPoll() != null
                    && pollId.equals(currentOption.getPoll().getId())) {
                existingVote = v;
                break;
            }
        }

        if (existingVote == null) {
            return null; // user has not voted in this poll
        }

        // If the existing vote already points to the same option, nothing to do
        if (existingVote.getOption() != null
                && newOption.getId().equals(existingVote.getOption().getId())) {
            return existingVote; // no change
        }

        // Remove vote from old option's vote set
        VoteOption oldOption = existingVote.getOption();
        if (oldOption != null) {
            oldOption.getVotes().remove(existingVote);
            voteOptionRepository.save(oldOption); // Update old option
        }

        // Assign the new option and add to its votes set
        existingVote.setOption(newOption);
        newOption.getVotes().add(existingVote);

        // Save the updated vote and option
        Vote updatedVote = voteRepository.save(existingVote);
        voteOptionRepository.save(newOption);

        // Invalidate caches
        redisCacheService.delete("poll_results", pollId);
        redisCacheService.delete("poll_votes", pollId);


        return updatedVote;
    }

    /**
     * Returns all votes in the system.
     *
     * @return List of all votes.
     */
    public List<Vote> getAllVotes() {
        /*  Before DB

         */
        return voteRepository.findAll();
    }

    /**
     * Returns all votes for a specific option.
     *
     * @param optionId The option ID.
     * @return List of votes or null.
     */
    public List<Vote> getVotesByOption(UUID optionId) {
        /*  Before DB

         */
        VoteOption option = voteOptionRepository.findById(optionId).orElse(null);
        if (option == null) return null;

        return new ArrayList<>(option.getVotes());
    }

    /**
     * Aggregates vote counts for a poll by collecting all votes from its options.
     * Uses a cache-first lookup.
     *
     * @param pollId The poll ID.
     * @return List of votes.
     */
    public List<Vote> getVotesByPoll(UUID pollId) {
        /*  Before DB

         */
        Object cachedVotes = redisCacheService.get("poll_votes", pollId, List.class);
        if (cachedVotes instanceof List) {
            return (List<Vote>) cachedVotes;
        }

        Poll poll = pollRepository.findById(pollId).orElse(null);
        if (poll == null) return Collections.emptyList();

        List<Vote> pollVotes = new ArrayList<>();
        for (VoteOption option : poll.getOptions()) {
            pollVotes.addAll(option.getVotes());
        }

        redisCacheService.cachePollVotes(pollId, pollVotes);
        return pollVotes;
    }

    /**
     * Fetches a vote by ID using a cache-first strategy.
     *
     * @param voteId The vote ID.
     * @return The vote or null.
     */
    public Vote getVoteById(UUID voteId) {
        /*  Before DB

         */
        Vote cachedVote = redisCacheService.get("vote", voteId, Vote.class);
        if (cachedVote != null) {
            return cachedVote;
        }

        Vote vote = voteRepository.findById(voteId).orElse(null);
        if (vote != null) {
            redisCacheService.cacheVote(voteId, vote);
        }
        return vote;
    }

    /**
     * Deletes a vote by ID, updates relational links,
     * invalidates caches, and returns the deleted vote.
     *
     * @param voteId The vote ID.
     * @return Deleted vote or null.
     */
    public Vote deleteVoteById(UUID voteId) {
        /*  Before DB

         */
        Vote vote = voteRepository.findById(voteId).orElse(null);
        if (vote != null) {
            UUID pollId = null;
            if (vote.getOption() != null && vote.getOption().getPoll() != null) {
                pollId = vote.getOption().getPoll().getId();
            }

            // Remove from option's votes
            if (vote.getOption() != null) {
                vote.getOption().getVotes().remove(vote);
                voteOptionRepository.save(vote.getOption());
            }
            // Remove from user's votes
            if (vote.getVoter() != null) {
                vote.getVoter().getVotes().remove(vote);
                userRepository.save(vote.getVoter());
            }
            // Delete the vote
            voteRepository.deleteById(voteId);

            // Invalidate caches
            redisCacheService.delete("vote", voteId);
            if (pollId != null) {
                redisCacheService.delete("poll_results", pollId);
                redisCacheService.delete("poll_votes", pollId);
            }
        }
        return vote;
    }

    /**
     * Counts votes for each option of a poll using repository aggregation.
     *
     * @param pollId The poll ID.
     * @return Map of optionId → voteCount.
     */
    public Map<UUID, Long> countVotesForPoll(UUID pollId) {
        List<VoteOption> options = voteOptionRepository.findByPollId(pollId);

        Map<UUID, Long> votesPerOption = new HashMap<>();
        for (VoteOption option : options) {
            long count = voteRepository.countByOption_Id(option.getId());
            votesPerOption.put(option.getId(), count);
        }

        return votesPerOption;
    }

    // ------- Manual cache clearing methods ---------------------------------
    // TODO not used but we can change manager to use them

    /**
     * Clears all cached data related to a specific poll.
     *
     * @param pollId The poll ID.
     */
    public void clearPollCache(UUID pollId) {
        redisCacheService.delete("poll", pollId);
        redisCacheService.delete("poll_results", pollId);
        redisCacheService.delete("poll_votes", pollId);
    }

    /**
     * Clears global caches for all polls and all users.
     */
    public void clearAllCache() {
        redisCacheService.delete("all_polls", null);
        redisCacheService.delete("all_users", null);
    }
}
