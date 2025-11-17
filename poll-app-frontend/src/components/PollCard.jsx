import React, { useState, useEffect } from "react";
import "../styles/PollCard.css";
import { Trash2 } from "lucide-react";
import {
    addOption,
    deleteOption,
    getPollResults
} from "../apiConfig/pollApi";

const PollCard = ({ poll, onDelete, currentUser }) => {

    const isExpired =
        !poll.validUntil ||
        new Date(poll.validUntil) < new Date() ||
        !poll.options ||
        poll.options.length === 0;

    const [options, setOptions] = useState(
        poll.options
            ? poll.options.map(o => ({ ...o, votesCount: 0 }))
            : []
    );

    const [editMode, setEditMode] = useState(false);
    const [newOption, setNewOption] = useState("");

    // ---------------- load vote counts ----------------
    useEffect(() => {
        const loadVotes = async () => {
            try {
                const results = await getPollResults(poll.id);
                setOptions(prev =>
                    prev.map(o => ({ ...o, votesCount: results[o.id] || 0 }))
                );
            } catch (err) {
                console.error("Failed loading vote counts", err);
            }
        };

        loadVotes();
    }, [poll.id]);

    // ----------------add options ----------------
    const handleAddOption = async () => {
        const trimmed = newOption.trim();
        if (!trimmed) return alert("Option cannot be empty");

        if (options.some(o => o.caption === trimmed))
            return alert("Option already exists");

        try {
            const created = await addOption(poll.id, {
                caption: trimmed,
                presentationOrder: options.length + 1
            });

            setOptions([...options, { ...created, votesCount: 0 }]);
            setNewOption("");
        } catch (error) {
            console.error(error);
            alert("Failed to add option");
        }
    };

    // ----------------delete option ----------------
    const handleDeleteOption = async opt => {
        try {
            if (opt.id) await deleteOption(poll.id, opt.id);

            setOptions(prev => prev.filter(o => o.id !== opt.id));
        } catch (error) {
            console.error("Failed to delete option:", error);
            alert("Error deleting option");
        }
    };

    //----------------delete whole poll----------------
    const handleDeletePoll = () => {
        if (window.confirm("Are you sure you want to delete this poll?")) {
            if (onDelete) onDelete(poll.id);
        }
    };

    return (
        <div className={`poll-card ${isExpired ? "expired" : ""}`}>
            <div className="poll-header">
                <div className="poll-header-text">
                    <h2 className="poll-question">{poll.question}</h2>
                    <p className="poll-meta">
                        Created by{" "}
                        <strong>{poll.createdBy?.username || "Unknown"}</strong>{" "}
                        | Valid until:{" "}
                        {poll.validUntil
                            ? new Date(poll.validUntil).toLocaleDateString()
                            : "N/A"}
                    </p>
                </div>

                {isExpired && <span className="expired-label">Closed</span>}
            </div>

            {/* ----------------list options---------------- */}
            <div className="poll-body">
                <div className="poll-options">
                    {options.map(opt => (
                        <div key={opt.id} className="poll-option">
                            <span>
                                {opt.caption} ({opt.votesCount} votes)
                            </span>

                            {editMode && (
                                <Trash2
                                    size={18}
                                    className="delete-option-icon"
                                    onClick={() => handleDeleteOption(opt)}
                                />
                            )}
                        </div>
                    ))}
                </div>

                {/* ----------------add option in edit mode---------------- */}
                {editMode && (
                    <div className="add-option">
                        <input
                            type="text"
                            placeholder="Add a new option"
                            value={newOption}
                            onChange={e => setNewOption(e.target.value)}
                            className="new-option-input"
                        />
                        <button className="add-option-btn" onClick={handleAddOption}>
                            Add
                        </button>
                    </div>
                )}

                {/* ----------------buttons---------------- */}
                <div className="poll-buttons">
                    {!editMode ? (
                        <>
                            {currentUser?.id === poll.createdBy?.id && (
                                <button
                                    className="edit-btn"
                                    onClick={() => setEditMode(true)}
                                >
                                    Edit
                                </button>
                            )}

                            {currentUser?.id === poll.createdBy?.id && (
                                <button className="delete-btn" onClick={handleDeletePoll}>
                                    <Trash2 size={16} /> Delete
                                </button>
                            )}
                        </>
                    ) : (
                        <>
                            <button
                                className="save-btn"
                                onClick={() => setEditMode(false)}
                            >
                                Save
                            </button>

                            <button
                                className="cancel-btn"
                                onClick={() => setEditMode(false)}
                            >
                                Cancel
                            </button>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default PollCard;