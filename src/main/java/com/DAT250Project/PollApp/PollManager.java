package com.DAT250Project.PollApp;

import com.DAT250Project.PollApp.model.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;


import com.DAT250Project.PollApp.model.*;
import com.DAT250Project.PollApp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

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

    // Constructor (optional) - Spring will handle dependency injection
    public PollManager() {}

    //------------------------------------------------ USER ------------------------------------------------------------

    // Create user
    public User createUser(User user) {
        // No need to generate ID manually - JPA will handle it with @GeneratedValue
        // Store the user in database using repository
        return userRepository.save(user);
    }

    // Get all users
    public List<User> getAllUsers() {
        // Return a list containing all user objects from database
        return userRepository.findAll();
    }

    // Get a user by id
    public User getUserById(UUID userId) {
        // Retrieve a user by ID from database, or null if not found
        return userRepository.findById(userId).orElse(null);
    }

    // Delete a user by id
    public User deleteUserById(UUID userId) {
        // Find user first to return it, then delete from database
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            userRepository.deleteById(userId);
        }
        return user;
    }

    // Get polls of a user
    public List<Poll> getPollsByUser(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;
        // Convert the Set<Poll> to List<Poll> because controller methods return a List<>, not a Set<>
        return new ArrayList<>(user.getCreatedPolls());
    }

    //Get votes of a user
    public List<Vote> getVotesByUser(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;
        // Convert the Set<Poll> to List<Poll> because controller methods return a List<>, not a Set<>
        return new ArrayList<>(user.getVotes());
    }

    //------------------------------------------------- POLL -----------------------------------------------------------

    // Create a poll
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
        Poll savedPoll = pollRepository.save(poll);

        // Set presentation order for options
        int order = 1;
        if (savedPoll.getOptions() != null) {
            for (VoteOption option : savedPoll.getOptions()) {
                option.setPresentationOrder(order++);
                // Options will be saved automatically because cascade is configured on Poll entity
            }
        }

        // Return the new poll
        return savedPoll;
    }

    // Get all polls
    public List<Poll> getAllPolls() {
        // Return all polls as a list from database
        return pollRepository.findAll();
    }

    // Get a poll by id
    public Poll getPollById(UUID pollId) {
        // Find a poll by ID from database, or null if not found
        return pollRepository.findById(pollId).orElse(null);
    }

    // Delete a poll by id
    public Poll deletePollById(UUID pollId) {

        // Find the poll first to return it
        Poll poll = pollRepository.findById(pollId).orElse(null);

        // If found, delete from database - cascading will handle related options
        // todo revise cascade problems
        if (poll != null) {
            pollRepository.deleteById(pollId);
        }

        return poll;
    }

    // Add an option to a poll
    public VoteOption addOptionToPoll(UUID pollId, VoteOption option) {
        /*  Before DB

         */
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

        return savedOption;
    }

    // Get all options of a poll
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
        VoteOption voteOption = voteOptionRepository.findById(optionId).orElse(null);
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

    // Create a new Vote for a given pollId, voterId and optionId
    public Vote createVote(UUID pollId, UUID voterId, UUID optionId) {
        /*  Before DB

         */
        // Validate existence of poll, user and option from database
        Poll poll = pollRepository.findById(pollId).orElse(null);
        User voter = userRepository.findById(voterId).orElse(null);
        VoteOption option = voteOptionRepository.findById(optionId).orElse(null);

        if (poll == null || voter == null || option == null) {
            return null; // any missing resource -> fail (controller will return 404 / 400)
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
        vote.setVoter(voter);                   // set user reference
        vote.setOption(option);                  // set chosen option

        // Persist in database
        Vote savedVote = voteRepository.save(vote);

        // Add the vote to the voter's vote set
        voter.getVotes().add(savedVote);
        userRepository.save(voter); // Update user to maintain consistency

        // Add the vote to the option's vote set
        option.getVotes().add(savedVote);
        voteOptionRepository.save(option); // Update option to maintain consistency

        return savedVote;
    }

    // Update a user's vote in a poll: change their chosen option to newOptionId
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

        return updatedVote;
    }

    // Get all votes
    public List<Vote> getAllVotes() {
        /*  Before DB

         */
        return voteRepository.findAll();
    }

    // Get the votes for an option
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
        Poll poll = pollRepository.findById(pollId).orElse(null);
        if (poll == null) return Collections.emptyList();

        List<Vote> pollVotes = new ArrayList<>();
        for (VoteOption option : poll.getOptions()) {
            pollVotes.addAll(option.getVotes());
        }
        return pollVotes;
    }

    // Get a vote by id
    public Vote getVoteById(UUID voteId) {
        /*  Before DB

         */
        return voteRepository.findById(voteId).orElse(null);
    }

    // Delete vote by id
    public Vote deleteVoteById(UUID voteId) {
        /*  Before DB

         */
        Vote vote = voteRepository.findById(voteId).orElse(null);
        if (vote != null) {
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
        }
        return vote;
    }
}