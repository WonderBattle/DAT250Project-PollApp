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

@Service
public class PollManager {

    // === JPA Repositories to replace in-memory data stores ===
    @Autowired
    private UserRepository userRepository;           // replaces Map<UUID, User> users
    @Autowired
    private PollRepository pollRepository;           // replaces Map<UUID, Poll> polls
    @Autowired
    private VoteOptionRepository voteOptionRepository; // replaces Map<UUID, VoteOption> options
    @Autowired
    private VoteRepository voteRepository;           // replaces Map<UUID, Vote> votes
    @Autowired
    private UserPublisher userPublisher;
    @Autowired
    private PollPublisher pollPublisher;
    @Autowired
    private VotePublisher votePublisher;

    // Injects the Redis cache service for caching operations
    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Constructor (optional) - Spring will handle dependency injection
    public PollManager() {}

    //------------------------------------------------ USER ------------------------------------------------------------

    // Create user with password encoding and cache invalidation
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

    // Get all users with cache-first strategy
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

    // Get a user by id with cache-first strategy
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

    // Delete a user by id with cache cleanup
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

    // Get user's polls with caching
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

    //Get votes of a user (no caching - votes change frequently)
    public List<Vote> getVotesByUser(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;
        // Convert the Set<Poll> to List<Poll> because controller methods return a List<>, not a Set<>
        return new ArrayList<>(user.getVotes());
    }

    //------------------------------------------------- POLL -----------------------------------------------------------

    // Create a poll and handles cache invalidation
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
        }else{
            //TODO respone not user defined
            return null;
        }

        // Save the poll in the database - this will cascade to options
        //Poll savedPoll = pollRepository.save(poll);

        // Set presentation order for options
        /*
        int order = 1;
        if (savedPoll.getOptions() != null) {
            for (VoteOption option : savedPoll.getOptions()) {
                option.setPresentationOrder(order++);
                option.setPoll(savedPoll); //trying to fix error while creating a poll
                // Options will be saved automatically because cascade is configured on Poll entity
            }
        }

         */

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

    // Get all polls  with cache-first strategy
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

    // Get public polls
    public List<Poll> getPublicPolls() {
        return pollRepository.findByPublicPollTrue();
    }

    // Get private polls
    public List<Poll> getPrivatePolls(UUID userId) {
        return pollRepository.findByPublicPollFalseAndCreatedBy_Id(userId);
    }

    // Get a poll by id
    // Retrieves a poll by ID with cache-first strategy
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

    // Delete a poll by id
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

    // Add an option to a poll with cache invalidation
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

    //Delete an option from a poll with cache invalidation
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

    // Get all options of a poll (no caching - part of poll entity)
    public List<VoteOption> getAllOptionsByPoll(UUID pollId) {
        /*  Before DB

         */
        Poll poll = pollRepository.findById(pollId).orElse(null);
        if (poll == null) return null;
        // Return the list of options for this poll
        return poll.getOptions();
    }

    // Get an option by id
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

    // Check if an option belongs to a poll
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

    // Check if the option exists and belongs to the poll with given ID
    /*
    public boolean optionBelongsToPoll(UUID optionId, UUID pollId) {
        VoteOption option = voteOptionRepository.findById(optionId).orElse(null);   // fetch option by id from database
        if (option == null) return false;            // option not found -> false
        Poll poll = pollRepository.findById(pollId).orElse(null);               // fetch poll by id from database
        if (poll == null) return false;              // poll not found -> false

        // The vote option stores the poll it belongs to; compare by id
        return option.getPoll() != null && pollId.equals(option.getPoll().getId());
    }
    */

    // Create a new Vote for a given pollId, voterId and optionId and handles cache invalidation for affected data
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

    // Update a user's vote in a poll: change their chosen option to newOptionId with cache invalidation
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

    // Get all votes (no caching - frequently changing)
    public List<Vote> getAllVotes() {
        /*  Before DB

         */
        return voteRepository.findAll();
    }

    // Get the votes for an option (no caching)
    public List<Vote> getVotesByOption(UUID optionId) {
        /*  Before DB

         */
        VoteOption option = voteOptionRepository.findById(optionId).orElse(null);
        if (option == null) return null;

        return new ArrayList<>(option.getVotes());
    }

    // Get the votes for a poll
    // Return list of votes for a poll by aggregating votes from each option
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

    // Get a vote by id with caching
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

    // Delete vote by id with cache cleanup
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
    //counting votes
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

    public void clearPollCache(UUID pollId) {
        redisCacheService.delete("poll", pollId);
        redisCacheService.delete("poll_results", pollId);
        redisCacheService.delete("poll_votes", pollId);
    }

    public void clearAllCache() {
        redisCacheService.delete("all_polls", null);
        redisCacheService.delete("all_users", null);
    }
}