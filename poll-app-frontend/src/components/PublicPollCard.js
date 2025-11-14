import React from "react";
import "../styles/PollCard.css";
import { useNavigate } from "react-router-dom";

const PublicPollCard = ({ poll, onVote }) => {
    const navigate = useNavigate();
    const isExpired = !poll.validUntil || new Date(poll.validUntil) < new Date() || !poll.options || poll.options.length === 0;

    // -------------------vote button-----------------
    const handleVoteClick = () => {
        if (!isExpired && onVote) {
            onVote(poll.id);
        }
    };

    return (
        <div className={`poll-card ${isExpired ? "expired" : ""}`}>
            <div className="poll-header">
                <div className="poll-header-text">
                    <h2 className="poll-question">{poll.question}</h2>
                    <p className="poll-meta">
                        Created by <strong>{poll.createdBy?.username || "Unknown"}</strong> | Valid until:{" "}
                        {poll.validUntil ? new Date(poll.validUntil).toLocaleDateString() : "N/A"}
                    </p>
                </div>

                {isExpired && <span className="expired-label">Closed</span>}
            </div>

            <div className="poll-buttons">
                <button
                    className={`vote-btn ${isExpired ? "disabled" : ""}`}
                    onClick={handleVoteClick}
                    disabled={isExpired}
                >
                    {isExpired ? "Voting Closed" : "Let's Vote"}
                </button>
            </div>
        </div>
    );
};

export default PublicPollCard;