import React from "react";
import "../styles/PollCard.css";
import { useNavigate } from "react-router-dom";

/**
 * PublicPollCard component displays a public poll with its question, creator, expiration date,
 * and a vote button (disabled if the poll is expired).
 *
 * @component
 * @param {Object} props - Component props
 * @param {Object} props.poll - Poll data object
 * @param {string} props.poll.id - Poll ID
 * @param {string} props.poll.question - Poll question
 * @param {string} props.poll.validUntil - Poll expiration date
 * @param {Array} props.poll.options - Array of poll options
 * @param {Object} props.poll.createdBy - User who created the poll
 * @param {Function} [props.onVote] - Optional callback function when user votes
 * @returns {JSX.Element} Rendered PublicPollCard component
 */
const PublicPollCard = ({ poll, onVote }) => {
    const navigate = useNavigate();

    /** Boolean flag indicating if the poll is expired */
    const isExpired =
        !poll.validUntil ||
        new Date(poll.validUntil) < new Date() ||
        !poll.options ||
        poll.options.length === 0;

    /**
     * Handles clicking the vote button.
     * Navigates to the public vote page if the poll is not expired.
     */
    const handleVoteClick = () => {
        if (!isExpired) {
            navigate(`/vote/public/${poll.id}`);
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