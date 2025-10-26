import React, { useState } from "react";
import "../styles/VotingCard.css";
import { Trash2 } from "lucide-react";

//----------------------------constants variables-------------------------------
const VotingCard = ({ poll }) => {
    const [selectedOption, setSelectedOption] = useState(null);
    const [options, setOptions] = useState(poll.options || []);
    const [newOption, setNewOption] = useState("");
    const [editMode, setEditMode] = useState(false);

    //----------------------------voting-------------------------------
    const handleVote = () => {
        if (!selectedOption) return alert("Please select an option!");
        alert(`You voted for: ${selectedOption}`);
    };

    //----------------------------edit mode for editing vote options-------------------------------
    const handleEdit = () => setEditMode(true);

    //----------------------------saving changes-------------------------------
    const handleSave = () => {
        setEditMode(false);
        alert("Changes saved!");
    };

    //----------------------------adding a new vote option-------------------------------
    const handleAddOption = () => {
        const trimmed = newOption.trim();
        if (!trimmed) return alert("Option cannot be empty");
        if (options.includes(trimmed)) return alert("Option already exists");
        setOptions([...options, trimmed]);
        setNewOption("");
    };

    //----------------------------deleting any vote option-------------------------------
    const handleDeleteOption = (opt) => {
        setOptions(options.filter((o) => o !== opt));
    };

    //----------------------------returning html-------------------------------
    return (
        <div className="poll-card">
            <div className="poll-header">
                <div className="poll-header-text">
                    <h2 className="poll-question">{poll.question}</h2>
                    <p className="poll-meta">
                        Created by <strong>{poll.createdBy}</strong> on{" "}
                        {new Date(poll.publishedAt).toLocaleDateString()} | Valid until:{" "}
                        {new Date(poll.validUntil).toLocaleDateString()}
                    </p>
                </div>
            </div>

            <div className="poll-body">
                <div className="poll-options">
                    {options.map((opt, idx) => (
                        <label key={idx} className="poll-option">
                            <input
                                type="radio"
                                name={`poll-${poll.id}`}
                                value={opt}
                                checked={selectedOption === opt}
                                onChange={() => setSelectedOption(opt)}
                                disabled={editMode}
                            />
                            <span>{opt}</span>
                            {editMode && (
                                <Trash2
                                    size={18}
                                    className="delete-option-icon"
                                    onClick={() => handleDeleteOption(opt)}
                                />
                            )}
                        </label>
                    ))}
                </div>

                {editMode && (
                    <div className="add-option">
                        <input
                            type="text"
                            placeholder="Add a new option"
                            value={newOption}
                            onChange={(e) => setNewOption(e.target.value)}
                            className="new-option-input"
                        />
                        <button className="add-option-btn" onClick={handleAddOption}>
                            Add
                        </button>
                    </div>
                )}

                <div className="poll-buttons">
                    {!editMode ? (
                        <>
                            <button className="vote-btn" onClick={handleVote}>
                                Vote
                            </button>
                            <button className="edit-btn" onClick={handleEdit}>
                                Edit Vote
                            </button>
                        </>
                    ) : (
                        <button className="save-btn" onClick={handleSave}>
                            Save
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
};

export default VotingCard;