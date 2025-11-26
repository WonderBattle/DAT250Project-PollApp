import React, { useEffect, useState } from "react";
import PublicPollCard from "../components/PublicPollCard";
import Header from "../components/Header";
import "../styles/Dashboard.css";
import { useNavigate } from "react-router-dom";
import {getAllPolls, getAllPublicPolls} from "../apiConfig/pollApi";

/**
 * PublicDashboardPage displays all public polls.
 * Users can toggle between active and expired polls and navigate to vote on each poll.
 *
 * @component
 * @returns {JSX.Element} Rendered PublicDashboardPage
 */
const PublicDashboardPage = () => {
    /** List of all public polls */
    const [polls, setPolls] = useState([]);

    /** Flag to show only active polls */
    const [showActiveOnly, setShowActiveOnly] = useState(true);

    /** Router navigation function */
    const navigate = useNavigate();

    /** Logged-in user info (if any) */
    const user = JSON.parse(localStorage.getItem("user"));

    useEffect(() => {
        const fetchPolls = async () => {
            try {
                const data = await getAllPublicPolls();
                setPolls(data);
            } catch (error) {
                console.error("Error fetching public polls:", error);
            }
        };
        fetchPolls();
    }, []);

    const filteredPolls = polls.filter((poll) => {
        const now = new Date();
        const validUntil = poll.validUntil ? new Date(poll.validUntil) : null;
        const isActive = validUntil ? validUntil > now : false;
        const hasOptions = poll.options && poll.options.length > 0;
        const expired = !isActive || !hasOptions;
        return showActiveOnly ? !expired : expired;
    });

    /**
     * Handles navigation to voting page for a public poll
     * @param {string} pollId - ID of the poll to vote on
     */
    const handleVoteClick = (pollId) => {
        window.location.href = `/vote/public/${pollId}`;
    };

    return (
        <div className="desktop-1">
            <Header />
            <main className="main-content">
                <h1 className="page-title">Public Polls</h1>

                {user ? (
                    <button className="back-btn" onClick={() => navigate("/dashboard")}>
                        ← Back to My Dashboard
                    </button>
                ) : (
                    <button className="back-btn" onClick={() => navigate("/")}>
                        ← Back to Home
                    </button>
                )}

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

                {filteredPolls.length > 0 ? (
                    filteredPolls.map((poll) => (
                        <PublicPollCard
                            key={poll.id}
                            poll={poll}
                            onVote={handleVoteClick}
                        />
                    ))
                ) : (
                    <p>No {showActiveOnly ? "active" : "expired"} polls found.</p>
                )}
            </main>
        </div>
    );
};

export default PublicDashboardPage;