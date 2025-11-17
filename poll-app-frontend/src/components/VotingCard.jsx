import React, { useEffect, useState } from "react";
import "../styles/VotingCard.css";
import { createVoteApi, updateVoteApi, getPollResults } from "../apiConfig/pollApi";

const VotingCard = ({ poll }) => {
    //----------------------------constants variables-------------------------------
    const loggedUser = JSON.parse(localStorage.getItem("user"));
    const userId = loggedUser?.id;

    const [options, setOptions] = useState(
        poll.options.map((o) => ({ ...o, votes: o.votes || [] }))
    );
    const [selectedOptionId, setSelectedOptionId] = useState(null);
    const [existingVoteOptionId, setExistingVoteOptionId] = useState(null);
    const [alreadyVoted, setAlreadyVoted] = useState(false);
    const [editMode, setEditMode] = useState(false);

    const isExpired =
        !poll.validUntil || new Date(poll.validUntil) < new Date() || !poll.options || poll.options.length === 0;

    // ----------------check if the user already voted ----------------
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

    // -------------load vote counts------------------------
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

    //----------------------------voting-------------------------------
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

    //----------------------------saving changes-------------------------------
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

//------------------cancel edited votes-------------------------
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