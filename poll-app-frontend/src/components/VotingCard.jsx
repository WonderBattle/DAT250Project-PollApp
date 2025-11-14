import React, { useEffect, useState } from "react";
import "../styles/VotingCard.css";
import { Trash2 } from "lucide-react";
import { addOption, deleteOption, createVoteApi } from "../apiConfig/pollApi";
import { getPollResults } from "../apiConfig/pollApi";


const VotingCard = ({ poll }) => {
    //----------------------------constants variables-------------------------------
    const loggedUser = JSON.parse(localStorage.getItem("user"));
    const [selectedOptionId, setSelectedOptionId] = useState(null);
    const [options, setOptions] = useState(
        poll.options ? poll.options.map((o) => ({ ...o, votes: o.votes || [] })) : []
    );
    const [newOption, setNewOption] = useState("");
    const [editMode, setEditMode] = useState(false);
    const [alreadyVoted, setAlreadyVoted] = useState(false);

    // ----------------check if the user already voted ----------------
    useEffect(() => {
        if (!loggedUser) return;

        const userId = loggedUser.id;

        const hasVoted = poll.options.some((opt) =>
            opt.votes?.some((v) => v.voterId === userId)
        );

        setAlreadyVoted(hasVoted);
    }, [poll, loggedUser]);

    useEffect(() => {
        const fetchVoteCounts = async () => {
            try {
                const results = await getPollResults(poll.id); // {optionId: voteCount}
                setOptions((prevOptions) =>
                    prevOptions.map((opt) => ({
                        ...opt,
                        votesCount: results[opt.id] || 0, // update votes count
                    }))
                );
            } catch (err) {
                console.error("Failed to fetch vote counts:", err);
            }
        };
        fetchVoteCounts();
    }, [poll.id]);

    //----------------------------voting-------------------------------
    const handleVote = async () => {
        if (!selectedOptionId) return alert("Please select an option!");

        try {
            await createVoteApi(poll.id, {
                voterId: loggedUser?.id || null,
                optionId: selectedOptionId,
            });

            alert("Vote submitted!");
            setAlreadyVoted(true);

            const results = await getPollResults(poll.id);
            setOptions((prevOptions) =>
                prevOptions.map((opt) => ({
                    ...opt,
                    votesCount: results[opt.id] || 0,
                }))
            );
        } catch (err) {
            if (err.response?.status === 409) {
                alert("You already voted in this poll.");
                setAlreadyVoted(true);
            } else {
                alert("Voting failed");
            }
        }
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
        if (options.some((o) => o.caption === trimmed))
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

    //----------------------------deleting any vote option-------------------------------
    const handleDeleteOption = async (opt) => {
        try {
            // delete from backend only if it exists in DB
            if (opt.id) {
                await deleteOption(poll.id, opt.id);
            }

            // remove from the UI
            setOptions((prev) => prev.filter((o) => o.id !== opt.id));

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

                {alreadyVoted && (
                    <span className="expired-label">You already voted</span>
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
                                disabled={editMode || alreadyVoted}
                            />
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
                            {!alreadyVoted && (
                                <button className="vote-btn" onClick={handleVote}>
                                    Vote
                                </button>
                            )}
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
