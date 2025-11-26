import React, { useEffect, useState } from "react";
import PollCard from "../components/PollCard";
import Header from "../components/Header";
import CreatePollCard from "../components/CreatePollCard";
import "../styles/Dashboard.css";
import { deletePoll, usersPoll } from "../apiConfig/pollApi";
import { useNavigate } from "react-router-dom";

/**
 * Dashboard page showing polls created by the current user.
 * Allows creating, deleting polls and toggling between active/expired polls.
 *
 * @component
 * @returns {JSX.Element} Rendered Dashboard component
 */
const Dashboard = () => {
    /** Array of polls fetched from the backend */
    const [polls, setPolls] = useState([]);

    /** Flag to show/hide the CreatePollCard modal */
    const [showCreatePollCard, setShowCreatePollCard] = useState(false);

    /** Current logged-in user object */
    const [currentUser, setCurrentUser] = useState(null);

    /** Flag to toggle between showing active or expired polls */
    const [showActiveOnly, setShowActiveOnly] = useState(true);

    const navigate = useNavigate();

    useEffect(() => {
        const storedUser = localStorage.getItem("user");
        if (storedUser) {
            setCurrentUser(JSON.parse(storedUser));
        }
    }, []);

    useEffect(() => {
        const fetchPolls = async () => {
            if (!currentUser) return;
            try {
                const data = await usersPoll(currentUser.id);
                setPolls(data);
            } catch (error) {
                console.error("Error fetching polls from backend:", error);
            }
        };
        fetchPolls();
    }, [currentUser]);

    /**
     * Deletes a poll both in backend and locally
     * @param {string} pollId - ID of the poll to delete
     */
    const handleDeletePoll = async (pollId) => {
        try {
            await deletePoll(pollId);
            setPolls((prev) => prev.filter((p) => p.id !== pollId));
        } catch (error) {
            console.error("Failed to delete poll:", error);
        }
    };

    /** Filter polls based on active/expired toggle */
    const filteredPolls = polls.filter((poll) => {
        const now = new Date();
        const validUntil = new Date(poll.validUntil);
        const isActive = validUntil > now;
        return showActiveOnly ? isActive : !isActive;
    });

    return (
        <div className="desktop-1">
            <Header />
            <main className="main-content">
                <h1 className="page-title">Poll Dashboard</h1>

                <button
                    className="vote-main-btn"
                    onClick={() => navigate("/votingpage")}
                >
                    Go to Voting Page
                </button>

                <div className="toggle-container">
                    <span className={!showActiveOnly ? "inactive" : ""}>Expired</span>
                    <label className="switch">
                        <input
                            type="checkbox"
                            checked={showActiveOnly}
                            onChange={() => setShowActiveOnly(!showActiveOnly)}
                        />
                        <span className="slider"></span>
                    </label>
                    <span className={showActiveOnly ? "active" : ""}>Active</span>
                </div>

                {!showCreatePollCard && (
                    <div className="create-poll-container">
                        <button
                            className="create-poll-btn"
                            onClick={() => setShowCreatePollCard(true)}
                        >
                            Create New Poll
                        </button>
                    </div>
                )}

                {showCreatePollCard && currentUser && (
                    <CreatePollCard
                        onCancel={() => setShowCreatePollCard(false)}
                        currentUser={currentUser}
                    />
                )}

                {filteredPolls.length > 0 ? (
                    filteredPolls.map((poll) => (
                        <PollCard
                            key={poll.id}
                            poll={poll}
                            onDelete={handleDeletePoll}
                            currentUser={currentUser}
                        />
                    ))
                ) : (
                    <p>No {showActiveOnly ? "active" : "expired"} polls found.</p>
                )}
            </main>
        </div>
    );
};

export default Dashboard;