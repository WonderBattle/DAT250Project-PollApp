import React, {useState} from "react";
import "../styles/VotingCard.css";
import {Trash2} from "lucide-react";

//----------------------------constants variables-------------------------------
const VotingCard = ({poll}) => {
    const [selectedOption, setSelectedOption] = useState(null);
    const [options, setOptions] = useState(
        poll.options ? poll.options.map((o) => ({ ...o, votes: o.votes || [] })) : []
    );
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
    const handleAddOption = async () => {
        const trimmed = newOption.trim();
        if (!trimmed) return alert("Option cannot be empty");
        if (options.some((o) => o.caption === trimmed)) return alert("Option already exists");

        try {
            const response = await fetch(`http://localhost:8080/polls/${poll.id}/options`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    caption: trimmed,
                    presentationOrder: options.length + 1
                })
            });

            if (!response.ok) throw new Error();

            const created = await response.json();

            setOptions([...options, created]); // backend returns the true object
            setNewOption("");

        } catch (error) {
            console.error(error);
            alert("Failed to add option");
        }
    };

    //----------------------------deleting any vote option-------------------------------
    const handleDeleteOption = async (opt) => {
        try {
            // if it's an existing option -> backend ID exists too
            if (opt.id) {
                await fetch(`http://localhost:8080/polls/${poll.id}/options/${opt.id}`, {
                    method: "DELETE",
                });
            }

            // remove from the UI
            setOptions(options.filter((o) => o.id !== opt.id));

        } catch (error) {
            console.error("Failed to delete option:", error);
            alert("Error deleting option");
        }
    };

    //----------------------------returning html-------------------------------
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
                    {options.map((opt, idx) => (
                        <label key={idx} className="poll-option">
                            <input
                                type="radio"
                                name={`poll-${poll.id}`}
                                value={opt.caption}
                                checked={selectedOption === opt.caption}
                                onChange={() => setSelectedOption(opt.caption)}
                                disabled={editMode}
                            />
                            <span>
      {opt.caption} ({opt.votes ? opt.votes.length : 0} votes)
    </span>
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
