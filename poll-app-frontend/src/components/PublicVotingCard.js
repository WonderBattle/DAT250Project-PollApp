import React, { useEffect, useState } from "react";
import "../styles/VotingCard.css";
import { createVoteApi, getPollResults } from "../apiConfig/pollApi";

const PublicVotingCard = ({ poll }) => {
    const [selectedOptionId, setSelectedOptionId] = useState(null);
    const [options, setOptions] = useState(
        poll.options ? poll.options.map((o) => ({ ...o, votesCount: o.votesCount || 0 })) : []
    );
    const [alreadyVoted, setAlreadyVoted] = useState(false);

    // ----------------check if user already voted today----------------
    useEffect(() => {
        const votedPolls = JSON.parse(localStorage.getItem("publicVotes") || "[]");
        setAlreadyVoted(votedPolls.includes(poll.id));
    }, [poll.id]);

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

    // ----------------handle voting----------------
    const handleVote = async () => {
        if (!selectedOptionId) return alert("Please select an option!");

        try {
            await createVoteApi(poll.id, {
                voterId: null, // no login required
                optionId: selectedOptionId,
            });

            alert("Vote submitted!");

            // mark as voted in localStorage
            const votedPolls = JSON.parse(localStorage.getItem("publicVotes") || "[]");
            localStorage.setItem("publicVotes", JSON.stringify([...votedPolls, poll.id]));
            setAlreadyVoted(true);

            // update votes count
            const results = await getPollResults(poll.id);
            setOptions((prevOptions) =>
                prevOptions.map((opt) => ({
                    ...opt,
                    votesCount: results[opt.id] || 0,
                }))
            );
        } catch (err) {
            if (err.response?.status === 409) {
                alert("You have already voted today.");
                setAlreadyVoted(true);
            } else {
                alert("Voting failed");
            }
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

                {alreadyVoted && (
                    <span className="expired-label">You already voted today</span>
                )}
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
                                disabled={alreadyVoted}
                            />
                            <span>
                                {opt.caption} ({opt.votesCount} votes)
                            </span>
                        </label>
                    ))}
                </div>

                <div className="poll-buttons">
                    {!alreadyVoted && (
                        <button className="vote-btn" onClick={handleVote}>
                            Vote
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
};

export default PublicVotingCard;