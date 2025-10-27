package com.DAT250Project.PollApp;

import com.DAT250Project.PollApp.model.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class PollManager {

    // === In-memory data stores ===
    private Map<UUID, User> users = new HashMap<>();  // stores all registered users.
    private Map<UUID, Poll> polls = new HashMap<>();  // stores all created polls.
    private Map<UUID, VoteOption> options = new HashMap<>();  // stores all vote options (each linked to a poll).
    private Map<UUID, Vote> votes = new HashMap<>();  // stores all votes (each linked to a user and an option).

    // Constructor (optional)
    public PollManager() {}

    //------------------------------------------------ USER ------------------------------------------------------------

    // Create user
    public User createUser(User user) {
        // Generate a new unique ID
        user.setId(UUID.randomUUID());
        // Store the user in memory
        users.put(user.getId(), user);
        // Return the created user
        return user;
    }

    // Get all users
    public List<User> getAllUsers() {
        // Return a list containing all user objects
        return new ArrayList<>(users.values());
    }

    // Get a user by id
    public User getUserById(UUID userId) {
        // Retrieve a user by ID, or null if not found
        return users.get(userId);
    }

    // Delete a user by id
    public User deleteUserById(UUID userId) {
        // Remove and return the user if exists, otherwise null
        return users.remove(userId);
    }

    // Get polls of a user
    public List<Poll> getPollsByUser(UUID userId) {
        User user = users.get(userId);
        if (user == null) return null;
        // Convert the Set<Poll> to List<Poll> because controller methods return a List<>, not a Set<>
        return new ArrayList<>(user.getCreatedPolls());
    }

    //Get votes of a user
    public List<Vote> getVotesByUser(UUID userId) {
        User user = users.get(userId);
        if (user == null) return null;
        // Convert the Set<Poll> to List<Poll> because controller methods return a List<>, not a Set<>
        return new ArrayList<>(user.getVotes());
    }

    //------------------------------------------------- POLL -----------------------------------------------------------

    // Create a poll
    public Poll createPoll(Poll poll) {
        // Assign a new unique ID to the poll
        poll.setId(UUID.randomUUID());

        // Assign validUntil and publishedAt
        poll.setPublishedAt(Instant.now());
        // if no validUntil is set by user, set it to +7 days
        if (poll.getValidUntil() == null) {
            poll.setValidUntil(Instant.now().plus(7, ChronoUnit.DAYS));
        }


        // Register the poll creator (if exists)
        User creator = poll.getCreatedBy();
        if (creator != null) {
            // If user exists in the map, use the same in-memory object
            User existingUser = users.get(creator.getId());
            if (existingUser == null) {
                users.put(creator.getId(), creator);
                existingUser = creator;
            }

            // Ensure bidirectional link uses the same object reference
            poll.setCreatedBy(existingUser);
            existingUser.getCreatedPolls().add(poll);
        }

        // Save the poll in the polls map
        polls.put(poll.getId(), poll);

        // Also register all options inside this poll
        int order = 1;
        if (poll.getOptions() != null) {
            for (VoteOption option : poll.getOptions()) {
                option.setId(UUID.randomUUID());
                option.setPoll(poll);
                option.setPresentationOrder(order++);
                options.put(option.getId(), option);
            }
        }

        // Return the new poll
        return poll;
    }

    // Get all polls
    public List<Poll> getAllPolls() {
        // Return all polls as a list
        return new ArrayList<>(polls.values());
    }

    // Get a poll by id
    public Poll getPollById(UUID pollId) {
        // Find a poll by ID, or null if not found
        return polls.get(pollId);
    }

    // Delete a poll by id
    public Poll deletePollById(UUID pollId) {
        // Remove the poll from the map
        Poll poll = polls.remove(pollId);

        // If found, clean up related data
        if (poll != null) {
            // Remove it from its creator
            User creator = poll.getCreatedBy();
            if (creator != null) {
                creator.getCreatedPolls().remove(poll);
            }

            if (poll.getOptions() != null) {
                // Remove all options belonging to this poll
                for (VoteOption option : poll.getOptions()) {
                    options.remove(option.getId());
                }
            }

        }
        return poll;
    }

    // Add an option to a poll
    public VoteOption addOptionToPoll(UUID pollId, VoteOption option) {
        // Find the target poll
        Poll poll = polls.get(pollId);
        if (poll == null) return null;

        // Create a unique ID for the new option
        option.setId(UUID.randomUUID());

        // Link it with the poll
        option.setPoll(poll);

        // Add it to the poll's option list
        poll.getOptions().add(option);

        // Save to global map
        options.put(option.getId(), option);

        return option;
    }

    // Get all options of a poll
    public List<VoteOption> getAllOptionsByPoll(UUID pollId) {
        Poll poll = polls.get(pollId);
        if (poll == null) return null;
        // Return the list of options for this poll
        return poll.getOptions();
    }

    // Get an option by id
    public VoteOption getOptionById(UUID optionId) {
        VoteOption voteOption = options.get(optionId);
        if (voteOption == null) return null;
        return voteOption;
    }

    //----------------------------------------------- VOTE -------------------------------------------------------------

    // Check if an option belongs to a poll
    public boolean optionBelongsToPoll(UUID optionId, UUID pollId) {
        VoteOption option = options.get(optionId);
        if (option == null) return false;
        Poll poll = polls.get(pollId);
        if (poll == null) return false;

        // Check if this option belongs to the given poll
        return poll.getOptions().contains(option);
    }

    // Check if the option exists and belongs to the poll with given ID
    /*
    public boolean optionBelongsToPoll(UUID optionId, UUID pollId) {
        VoteOption option = options.get(optionId);   // fetch option by id from global map
        if (option == null) return false;            // option not found -> false
        Poll poll = polls.get(pollId);               // fetch poll by id from global map
        if (poll == null) return false;              // poll not found -> false

        // The vote option stores the poll it belongs to; compare by id
        return option.getPoll() != null && pollId.equals(option.getPoll().getId());
    }
    */

    // Create a new Vote for a given pollId, voterId and optionId
    public Vote createVote(UUID pollId, UUID voterId, UUID optionId) {
        // Validate existence of poll, user and option
        Poll poll = polls.get(pollId);
        User voter = users.get(voterId);
        VoteOption option = options.get(optionId);

        if (poll == null || voter == null || option == null) {
            return null; // any missing resource -> fail (controller will return 404 / 400)
        }

        // Ensure the option actually belongs to the poll
        if (option.getPoll() == null || !pollId.equals(option.getPoll().getId())) {
            return null; // invalid relationship - controller should treat as bad request
        }

        // Create Vote instance using your existing Vote constructors
        Vote vote = new Vote();                  // uses default constructor
        vote.setId(UUID.randomUUID());           // assign id
        vote.setPublishedAt(Instant.now());      // record timestamp

        // Link vote to voter and option
        vote.setVoter(voter);                   // set user reference
        vote.setOption(option);                  // set chosen option

        // Persist in global vote map
        votes.put(vote.getId(), vote);

        // Add the vote to the voter's vote set
        voter.getVotes().add(vote);

        // Add the vote to the option's vote set
        option.getVotes().add(vote);

        // Do NOT set any poll on vote (model doesn't have that field)
        // Do NOT try to add the vote to poll (poll has no votes collection)

        return vote;
    }


    // Update a user's vote in a poll: change their chosen option to newOptionId
    public Vote updateVote(UUID pollId, UUID voterId, UUID newOptionId) {
        Poll poll = polls.get(pollId);
        User voter = users.get(voterId);
        VoteOption newOption = options.get(newOptionId);

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
        }

        // Assign the new option and add to its votes set
        existingVote.setOption(newOption);
        newOption.getVotes().add(existingVote);

        // Voter->votes map already contains this vote, so no change needed there

        return existingVote;
    }


    // Get all votes
    public List<Vote> getAllVotes() {
        return new ArrayList<>(votes.values());
    }

    // Get the votes for an option
    public List<Vote> getVotesByOption(UUID optionId) {
        VoteOption option = options.get(optionId);
        if (option == null) return null;

        return new ArrayList<>(option.getVotes());
    }

    // Get the votes for a poll
    // Return list of votes for a poll by aggregating votes from each option
    public List<Vote> getVotesByPoll(UUID pollId) {
        Poll poll = polls.get(pollId);
        if (poll == null) return Collections.emptyList();

        List<Vote> pollVotes = new ArrayList<>();
        for (VoteOption option : poll.getOptions()) {
            pollVotes.addAll(option.getVotes());
        }
        return pollVotes;
    }


    // Get a vote by id
    public Vote getVoteById(UUID voteId) {
        return votes.get(voteId);
    }

    // Delete vote by id
    public Vote deleteVoteById(UUID voteId) {
        Vote vote = votes.get(voteId);
        vote.getOption().getVotes().remove(vote);
        return votes.remove(voteId);
    }



}
