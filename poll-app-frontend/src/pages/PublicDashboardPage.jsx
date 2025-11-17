import React, { useEffect, useState } from "react";
import PublicPollCard from "../components/PublicPollCard";
import Header from "../components/Header";
import "../styles/Dashboard.css";
import { useNavigate } from "react-router-dom";
import { getAllPolls } from "../apiConfig/pollApi";

const PublicDashboardPage = () => {
    const [polls, setPolls] = useState([]);
    const [showActiveOnly, setShowActiveOnly] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchPolls = async () => {
            try {
                const data = await getAllPolls();
                setPolls(data);
            } catch (error) {
                console.error("Error fetching public polls:", error.message || error);
            }
        };
        fetchPolls();
    }, []);

    //-------------------toggle active/expired polls-------------------
    const filteredPolls = polls.filter((poll) => {
        const now = new Date();
        const validUntil = poll.validUntil ? new Date(poll.validUntil) : null;
        const isActive = validUntil ? validUntil > now : false;
        // if no validUntil or no options, treat as expired
        const hasOptions = poll.options && poll.options.length > 0;
        const expired = !isActive || !hasOptions;
        return showActiveOnly ? !expired === true : expired === true;
    });

    //-------------------vote click-------------------
    const handleVoteClick = (pollId) => {
        // For public polls, everyone can vote
        window.location.href = `/vote/public/${pollId}`;
    };

    return (
        <div className="desktop-1">
            <Header />
            <main className="main-content">
                <h1 className="page-title">Public Polls</h1>
                <button className="back-btn" onClick={() => navigate("/")}>
                    ← Back to Home
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