import React, { useState } from "react";
import "../styles/VotingCard.css";
import { Trash2 } from "lucide-react";
import {createPoll} from "../apiConfig/pollApi"

//----------------------frontend without any backend yet--------------------
const CreatePollCard = ({ onCancel, currentUser }) => {
    const [question, setQuestion] = useState("");
    const [expirationDate, setExpirationDate] = useState("");
    const [options, setOptions] = useState([
        { id: 1, caption: "" },
        { id: 2, caption: "" },
    ]);
    const [isPublicPoll, setIsPublicPoll] = useState(true);

    const today = new Date();
    today.setDate(today.getDate() + 1);
    const minDate = today.toISOString().split("T")[0];

    //----------------------add new empty option--------------------
    const handleAddOption = () => {
        setOptions([...options, { id: Date.now(), caption: "" }]);
    };

    //----------------------delete poll option (but min 2 should)--------------------
    const handleDeleteOption = (id) => {
        if (options.length <= 2) return alert("A poll must have at least 2 options.");
        setOptions(options.filter((opt) => opt.id !== id));
    };

    //----------------------update option text--------------------
    const handleOptionChange = (id, value) => {
        setOptions(options.map((opt) => (opt.id === id ? { ...opt, caption: value } : opt)));
    };

    //----------------------create poll button methods--------------------
    const handleCreatePoll = async () => {
        if (!question.trim()) return alert("Please enter the poll question.");
        if (!expirationDate) return alert("Please select an expiration date.");

        if (!currentUser || !currentUser.id) {
            console.error("No currentUser passed to CreatePollCard!");
            return <p>Loading user...</p>;
        }

        // expiration date must be in the future
        if (new Date(expirationDate) <= new Date()) {
            return alert("Expiration date must be in the future.");
        }

        if (options.some((opt) => !opt.caption.trim()))
            return alert("Please fill all option fields.");

        try {
            const payload = {
                question,
                validUntil: new Date(expirationDate).toISOString(),
                publicPoll: isPublicPoll,
                createdBy: { id: currentUser.id }, // must exist
                options: options.map(o => ({ caption: o.caption }))
            };

            console.log("Sending payload:", payload);

            const savedPoll = await createPoll(payload, currentUser?.token);

            console.log("Created Poll:", savedPoll);
            alert("Poll successfully created!");

            onCancel(); // close modal
        } catch (error) {
            console.error("Error creating poll:", error);
            alert("Failed to create poll.");
        }
    };

    return (
        <div className="poll-card">
            <div className="poll-header">
                <div className="poll-header-text">
                    <h2 className="poll-question">Create a New Poll</h2>
                    <p className="poll-meta">
                        Created by <strong>{currentUser?.username}</strong>| Expiration Date:{" "}
                        {expirationDate ? new Date(expirationDate).toLocaleDateString() : "Not set"}
                    </p>
                </div>
            </div>

            <div className="poll-body">
                <label style={{ fontWeight: "bold", marginBottom: "5px", display: "block" }}>
                    Expiration Date
                </label>
                <div className="add-option">
                    <input
                        type="date"
                        value={expirationDate}
                        min={minDate} // prevent selecting today or past dates
                        onChange={(e) => setExpirationDate(e.target.value)}
                        className="new-option-input"
                    />
                </div>

                <label style={{ fontWeight: "bold", marginBottom: "5px", display: "block", marginTop: "10px" }}>
                    Poll Question
                </label>
                <div className="add-option">
                    <input
                        type="text"
                        placeholder="Enter poll question"
                        value={question}
                        onChange={(e) => setQuestion(e.target.value)}
                        className="new-option-input"
                    />
                </div>

                <label style={{ fontWeight: "bold", marginBottom: "5px", display: "block", marginTop: "10px" }}>
                    Poll Options
                </label>
                <div className="poll-options">
                    {options.map((opt, idx) => (
                        <div key={opt.id} className="poll-option">
                            <span>
                                <input
                                    type="text"
                                    placeholder={`Option ${idx + 1}`}
                                    value={opt.caption}
                                    onChange={(e) => handleOptionChange(opt.id, e.target.value)}
                                    className="new-option-input"
                                />
                            </span>
                            <Trash2
                                size={18}
                                className="delete-option-icon"
                                onClick={() => handleDeleteOption(opt.id)}
                            />
                        </div>
                    ))}
                </div>

                <div className="add-option">
                    <button className="add-option-btn" onClick={handleAddOption}>
                        Add Option
                    </button>
                </div>
                <label className="font-bold mb-2 block mt-4">Poll Visibility</label>
                <div style={{ display: "flex", gap: "20px", alignItems: "center" }}>
                    <label>
                        <input
                            type="radio"
                            name="pollVisibility"
                            value="public"
                            checked={isPublicPoll === true}
                            onChange={() => setIsPublicPoll(true)}
                        />
                        Public Poll
                    </label>
                    <label>
                        <input
                            type="radio"
                            name="pollVisibility"
                            value="private"
                            checked={isPublicPoll === false}
                            onChange={() => setIsPublicPoll(false)}
                        />
                        Private Poll
                    </label>
                </div>

                <div className="poll-buttons">
                    <button className="save-btn" onClick={handleCreatePoll}>
                        Create Poll
                    </button>
                    <button className="delete-btn" onClick={onCancel}>
                        Cancel
                    </button>
                </div>
            </div>
        </div>
    );
};

export default CreatePollCard;