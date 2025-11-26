import React, { useEffect, useState } from "react";
import "../styles/VotingCard.css";
import { createVoteApi, getPollResults } from "../apiConfig/pollApi";

/**
 * PublicVotingCard renders a public poll where users can vote without logging in.
 * Displays the poll question, options with current vote counts and allows unlimited voting.
 *
 * @component
 * @param {Object} props - Component props
 * @param {Object} props.poll - Poll object containing metadata and options
 * @param {string} props.poll.id - Unique ID of the poll
 * @param {string} props.poll.question - Poll question text
 * @param {Array} props.poll.options - Array of poll options
 * @param {Object} props.poll.createdBy - User who created the poll
 * @param {string} props.poll.publishedAt - Poll creation date
 * @param {string} props.poll.validUntil - Poll expiration date
 * @returns {JSX.Element} Rendered PublicVotingCard
 */
const PublicVotingCard = ({ poll }) => {
    /** Selected option ID for voting */
    const [selectedOptionId, setSelectedOptionId] = useState(null);

    /** Poll options with votes count */
    const [options, setOptions] = useState(
        poll.options ? poll.options.map((o) => ({ ...o, votesCount: o.votesCount || 0 })) : []
    );

    // ----------------fetch vote counts----------------
    useEffect(() => {
        const fetchVoteCounts = async () => {
            try {
                const results = await getPollResults(poll.id); // {optionId: voteCount}
                setOptions((prevOptions) =>
                    prevOptions.map((opt) => ({
                        ...opt,
                        votesCount: results[opt.id] || 0,
                    }))
                );
            } catch (err) {
                console.error("Failed to fetch vote counts:", err);
            }
        };
        fetchVoteCounts();
    }, [poll.id]);

    /**
     * Handles voting for the selected option.
     * Updates vote counts after submitting.
     */
    const handleVote = async () => {
        if (!selectedOptionId) return alert("Please select an option!");

        try {
            await createVoteApi(poll.id, {
                voterId: null, // no login required
                optionId: selectedOptionId,
            });

            alert("Vote submitted!");

            const results = await getPollResults(poll.id);
            setOptions((prevOptions) =>
                prevOptions.map((opt) => ({
                    ...opt,
                    votesCount: results[opt.id] || 0,
                }))
            );
        } catch (err) {
            console.error("Voting failed", err);
            alert("Voting failed. Please try again.");
        }
    };

    return (
        <div className="poll-card">
            <div className="poll-header">
                <div className="poll-header-text">
                    <h2 className="poll-question">{poll.question}</h2>
                    <p className="poll-meta">
                        Created by <strong>{poll.createdBy?.username || "Unknown"}</strong> on{" "}
                        {poll.publishedAt ? new Date(poll.publishedAt).toLocaleDateString() : "N/A"} | Valid until:{" "}
                        {poll.validUntil ? new Date(poll.validUntil).toLocaleDateString() : "N/A"}
                    </p>
                </div>
            </div>

            <div className="poll-body">
                <div className="poll-options">
                    {options.map((opt) => (
                        <label key={opt.id} className="poll-option">
                            <input
                                type="radio"
                                name={`poll-${poll.id}`}
                                checked={selectedOptionId === opt.id}
                                onChange={() => setSelectedOptionId(opt.id)}
                            />
                            <span>
                                {opt.caption} ({opt.votesCount} votes)
                            </span>
                        </label>
                    ))}
                </div>

                <div className="poll-buttons">
                    <button className="vote-btn" onClick={handleVote}>
                        Vote
                    </button>
                </div>
            </div>
        </div>
    );
};

export default PublicVotingCard;