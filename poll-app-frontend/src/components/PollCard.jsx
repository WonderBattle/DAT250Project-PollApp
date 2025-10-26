import React, {useState} from "react";
import "../styles/PollCard.css";
import { ChevronDown, ChevronUp } from "lucide-react"; // install lucide-react for nice icons


const PollCard = ({poll, onVote, onDelete, onEdit}) => {
    const [expanded, setExpanded] = useState(false);
    const [selectedOption, setSelectedOption] = useState(null);
    const [votes, setVotes] = useState(poll.totalVotes || 0);

//-----------------voting on a poll --------------------
    const handleVote = () => {
        if (selectedOption) {
            console.log("Voted for: ", selectedOption);
            setVotes(votes + 1);
            if (onVote) onVote(poll.id, selectedOption);
        } else {
            alert("please select an option before voting");
        }
    };

//-------------------edit your own vote on a poll, I mean changing it-----------------
    const handleEditVote = () => {
        console.log("edit vite for poll:", poll.id);
        if (onEdit) onEdit(poll.id);
    };

//-------------------deleting a whole poll ------------------------
    const handleDelete = () => {
        console.log("deleting poll:", poll.id)
        if (onDelete) onDelete(poll.id)
    };

//-----------------HTML to visualize-------------------------------
    return (
        <div className="poll-card">
            {/* ----------------------poll card is folded-----------------------*/}
            <div className="poll-header" onClick={() => setExpanded(!expanded)}>
                <div className="poll-header-text">
                    <h2 className="poll-question">{poll.question}</h2>
                    <p className="poll-meta">
                        Created by <strong>{poll.createdBy}</strong> on{" "}
                        {new Date(poll.createdAt).toLocaleDateString()}
                    </p>
                </div>
                <div className="poll-arrow">
                    {expanded ? <ChevronUp size={28}/> : <ChevronDown size={28}/>}
                </div>
            </div>

            {/*----------------poll card is unfolded-------------------*/}
            {expanded && (
                <div className="poll-body">
                    <div className="poll-options">
                        {poll.options.map((opt, index) => (
                            <label key={index} className="poll-option">
                                <input
                                    type="radio"
                                    name={`poll-${poll.id}`}
                                    value={opt}
                                    checked={selectedOption === opt}
                                    onChange={() => setSelectedOption(opt)}
                                />
                                {opt}
                            </label>
                        ))}
                    </div>

                    <div className="poll-buttons">
                        <button className="vote-btn" onClick={handleVote}>
                            Vote
                        </button>
                        <button className="edit-btn" onClick={handleEditVote}>
                            Edit Vote
                        </button>
                        <button className="delete-btn" onClick={handleDelete}>
                            Delete
                        </button>
                    </div>
                    {/*----------------vote counter as display for now-------------------*/}
                    <div className="poll-footer">
                        <span className="vote-counter">🗳️ {votes} votes</span>
                    </div>
                </div>
            )}
        </div>);
};

export default PollCard;

