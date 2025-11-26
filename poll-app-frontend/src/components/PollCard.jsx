import React, { useState, useEffect } from "react";
import "../styles/PollCard.css";
import { Trash2 } from "lucide-react";
import {
    addOption,
    deleteOption,
    getPollResults,
    updatePollPrivacy
} from "../apiConfig/pollApi";

/**
 * PollCard component displays a poll with options, vote counts and editing functionality.
 *
 * @component
 * @param {Object} props - Component props
 * @param {Object} props.poll - Poll data object
 * @param {string} props.poll.id - Poll ID
 * @param {string} props.poll.question - Poll question
 * @param {string} props.poll.validUntil - Poll expiration date
 * @param {Array} props.poll.options - List of poll options
 * @param {boolean} props.poll.publicPoll - Poll visibility
 * @param {Object} props.poll.createdBy - User who created the poll
 * @param {Function} props.onDelete - Callback function to delete the poll
 * @param {Object} props.currentUser - Current logged-in user object
 * @returns {JSX.Element} The rendered PollCard component
 */
const PollCard = ({ poll, onDelete, currentUser }) => {
    /** Boolean flag to mark if poll is expired */
    const isExpired =
        !poll.validUntil ||
        new Date(poll.validUntil) < new Date() ||
        !poll.options ||
        poll.options.length === 0;

    /** Poll options state with vote counts */
    const [options, setOptions] = useState(
        poll.options ? poll.options.map(o => ({ ...o, votesCount: 0 })) : []
    );

    /** Flag to toggle edit mode */
    const [editMode, setEditMode] = useState(false);

    /** New option text input */
    const [newOption, setNewOption] = useState("");

    /** Poll visibility state */
    const [isPublic, setIsPublic] = useState(poll.publicPoll);
    const [originalVisibility, setOriginalVisibility] = useState(poll.publicPoll);

    /**
     * Load vote counts for each option on component mount.
     */
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

    /**
     * Adds a new option to the poll.
     */
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

    /**
     * Deletes a specific option from the poll.
     * @param {Object} opt - Option object to delete
     */
    const handleDeleteOption = async (opt) => {
        try {
            if (opt.id) await deleteOption(poll.id, opt.id);

            setOptions(prev => prev.filter(o => o.id !== opt.id));
        } catch (error) {
            console.error("Failed to delete option:", error);
            alert("Error deleting option");
        }
    };

    /**
     * Deletes the entire poll after confirmation.
     */
    const handleDeletePoll = () => {
        if (window.confirm("Are you sure you want to delete this poll?")) {
            if (onDelete) onDelete(poll.id);
        }
    };

    /**
     * Saves changes made in edit mode (visibility updates).
     */
    const handleSave = async () => {
        try {
            if (isPublic !== originalVisibility) {
                await updatePollPrivacy(poll.id, isPublic, currentUser.id);
            }
            setOriginalVisibility(isPublic);
            setEditMode(false);
        } catch (error) {
            console.error("Failed to update privacy", error);
            alert("Could not update poll visibility");
        }
    };

    /** Cancels editing and restores original visibility */
    const handleCancel = () => {
        setIsPublic(originalVisibility);
        setEditMode(false);
    };

    return (
        <div className={`poll-card ${isExpired ? "expired" : ""}`}>
            <div className="poll-header">
                <div className="poll-header-text">
                    <h2 className="poll-question">{poll.question}</h2>
                    <p className="poll-meta">
                        Created by <strong>{poll.createdBy?.username || "Unknown"}</strong> |
                        Valid until:{" "}
                        {poll.validUntil
                            ? new Date(poll.validUntil).toLocaleDateString()
                            : "N/A"}
                    </p>
                    <p className="poll-meta">
                        Visibility: <strong>{isPublic ? "Public" : "Private"}</strong>
                    </p>
                </div>
                {isExpired && <span className="expired-label">Closed</span>}
            </div>

            <div className="poll-body">
                {editMode && (
                    <div style={{marginTop: "10px", marginBottom: "20px"}}>
                        <label className="font-bold mb-2 block">Poll Visibility<br/></label>
                        <label>
                            <input
                                type="radio"
                                name="pollVisibility"
                                checked={isPublic === true}
                                onChange={() => setIsPublic(true)}
                            />
                            Public
                        </label>
                        <label style={{marginLeft: "20px"}}>
                            <input
                                type="radio"
                                name="pollVisibility"
                                checked={isPublic === false}
                                onChange={() => setIsPublic(false)}
                            />
                            Private
                        </label>
                    </div>
                )}

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

                <div className="poll-buttons">
                    {!editMode ? (
                        <>
                            {currentUser?.id === poll.createdBy?.id && (
                                <button className="edit-btn" onClick={() => setEditMode(true)}>
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
                            <button className="save-btn" onClick={handleSave}>
                                Save
                            </button>
                            <button className="cancel-btn" onClick={handleCancel}>
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