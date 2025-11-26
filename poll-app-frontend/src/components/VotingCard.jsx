import React, { useEffect, useState } from "react";
import "../styles/VotingCard.css";
import { createVoteApi, updateVoteApi, getPollResults } from "../apiConfig/pollApi";

/**
 * VotingCard renders a poll where a logged-in user can vote and update their vote.
 * Displays poll question, options and current vote counts.
 *
 * @component
 * @param {Object} props - Component props
 * @param {Object} props.poll - Poll object containing metadata and options
 * @param {string} props.poll.id - Unique ID of the poll
 * @param {string} props.poll.question - Poll question text
 * @param {Array} props.poll.options - Array of poll options
 * @param {Object} props.poll.createdBy - User who created the poll
 * @param {string} props.poll.validUntil - Poll expiration date
 * @returns {JSX.Element} Rendered VotingCard
 */
const VotingCard = ({ poll }) => {
    /** Currently logged-in user */
    const loggedUser = JSON.parse(localStorage.getItem("user"));
    const userId = loggedUser?.id;

    /** Poll options with vote data */
    const [options, setOptions] = useState(
        poll.options.map((o) => ({ ...o, votes: o.votes || [] }))
    );

    /** Currently selected option ID for voting */
    const [selectedOptionId, setSelectedOptionId] = useState(null);

    /** Option ID for which the user has already voted */
    const [existingVoteOptionId, setExistingVoteOptionId] = useState(null);

    /** Flag if the user has already voted */
    const [alreadyVoted, setAlreadyVoted] = useState(false);

    /** Flag to control edit mode for changing votes */
    const [editMode, setEditMode] = useState(false);

    /** Boolean to indicate if the poll has expired */
    const isExpired =
        !poll.validUntil ||
        new Date(poll.validUntil) < new Date() ||
        !poll.options ||
        poll.options.length === 0;

    useEffect(() => {
        const userVote = poll.options.find((opt) =>
            opt.votes?.some((v) => v.voterId === userId)
        );

        if (userVote) {
            setAlreadyVoted(true);
            setExistingVoteOptionId(userVote.id);
            setSelectedOptionId(userVote.id);
        }
    }, [poll, userId]);

    useEffect(() => {
        const loadCounts = async () => {
            try {
                const results = await getPollResults(poll.id);
                setOptions((prev) =>
                    prev.map((o) => ({ ...o, votesCount: results[o.id] || 0 }))
                );
            } catch (err) {
                console.error("Failed to load vote counts:", err);
            }
        };
        loadCounts();
    }, [poll.id]);

    /**
     * Handles submitting a vote for the selected option.
     * Calls backend API and updates local vote count state.
     */
    const handleVote = async () => {
        if (!selectedOptionId) return alert("Select an option first");

        try {
            await createVoteApi(poll.id, {
                voterId: userId,
                optionId: selectedOptionId,
            });

            alert("Vote submitted!");
            setAlreadyVoted(true);
            setExistingVoteOptionId(selectedOptionId);

            const results = await getPollResults(poll.id);
            setOptions((prev) =>
                prev.map((o) => ({ ...o, votesCount: results[o.id] || 0 }))
            );
        } catch (err) {
            if (err.response?.status === 409) {
                alert("You already voted!");
            } else {
                alert("Error submitting vote");
            }
        }
    };

    /**
     * Handles saving a changed vote when in edit mode.
     * Updates the vote in backend and refreshes vote counts.
     */
    const handleSave = async () => {
        if (!selectedOptionId) return alert("Select an option");

        if (selectedOptionId === existingVoteOptionId) {
            setEditMode(false);
            return;
        }

        try {
            await updateVoteApi(poll.id, {
                voterId: userId,
                optionId: selectedOptionId,
            });

            alert("Vote updated!");
            setExistingVoteOptionId(selectedOptionId);
            setEditMode(false);

            const results = await getPollResults(poll.id);
            setOptions((prev) =>
                prev.map((o) => ({ ...o, votesCount: results[o.id] || 0 }))
            );
        } catch (err) {
            alert("Error updating vote");
        }
    };

    /** Cancels editing vote and restores previous selection */
    const handleCancel = () => {
        setSelectedOptionId(existingVoteOptionId);
        setEditMode(false);
    };

    return (
        <div className={`poll-card ${isExpired ? "expired" : ""}`}>
            {alreadyVoted && !editMode && !isExpired && (
                <span className="voted-label">You already voted</span>
            )}
            {isExpired && <span className="expired-label">Closed</span>}

            <h2>{poll.question}</h2>
            <p className="poll-meta">
                Created by <strong>{poll.createdBy?.username || "Unknown"}</strong> |{" "}
                Valid until: {poll.validUntil ? new Date(poll.validUntil).toLocaleDateString() : "N/A"}
            </p>

            <div className="poll-options">
                {options.map((opt) => (
                    <label key={opt.id} className="poll-option">
                        <input
                            type="radio"
                            name={`poll-${poll.id}`}
                            checked={selectedOptionId === opt.id}
                            onChange={() => setSelectedOptionId(opt.id)}
                            disabled={!editMode && (alreadyVoted || isExpired)}
                        />
                        {opt.caption} ({opt.votesCount || 0} votes)
                    </label>
                ))}
            </div>

            {!isExpired && (
                <div className="poll-buttons">
                    {!editMode ? (
                        <>
                            {!alreadyVoted && <button className="vote-btn" onClick={handleVote}>Vote</button>}
                            {alreadyVoted && (
                                <button className="edit-btn" onClick={() => setEditMode(true)}>Edit Vote</button>
                            )}
                        </>
                    ) : (
                        <>
                            <button className="save-btn" onClick={handleSave}>Save</button>
                            <button className="cancel-btn" onClick={handleCancel}>Cancel</button>
                        </>
                    )}
                </div>
            )}
        </div>
    );
};

export default VotingCard;