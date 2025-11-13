import React from "react";
import "../styles/PollCard.css";
import { Trash2 } from "lucide-react";
import {useNavigate} from "react-router-dom";

const PollCard = ({ poll, onDelete }) => {

    const navigate = useNavigate();
    const isExpired = !poll.validUntil || new Date(poll.validUntil) < new Date() || !poll.options || poll.options.length === 0;

    //-------------------delete whole poll -----------------
    const handleDeletePoll = () => {
        if (window.confirm("Are you sure you want to delete this poll?")) {
            if (onDelete) onDelete(poll.id);
        }
    };

    //-------------------vote button click -----------------
    const handleVoteClick = () => {
        if (!isExpired) {
            navigate(`/vote/${poll.id}`);
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
                <button className="delete-btn" onClick={handleDeletePoll}>
                    <Trash2 size={16} /> Delete
                </button>
            </div>

        </div>
    );
};

export default PollCard;