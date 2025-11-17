import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../components/Header";
import VotingCard from "../components/VotingCard";
import "../styles/VotingPage.css";
import { getAllPolls } from "../apiConfig/pollApi";

const VotingPage = () => {
    const [polls, setPolls] = useState([]);
    const [showActiveOnly, setShowActiveOnly] = useState(true);
    const navigate = useNavigate();

    const loggedUser = JSON.parse(localStorage.getItem("user"));

    useEffect(() => {
        const fetchPolls = async () => {
            try {
                const data = await getAllPolls();
                setPolls(data);
            } catch (error) {
                console.error("Error loading polls:", error);
            }
        };
        fetchPolls();
    }, []);

    // ----------- filter active / expired polls -----------
    const filteredPolls = polls.filter(poll => {
        const now = new Date();
        const validUntil = new Date(poll.validUntil);
        const isActive = validUntil > now;
        return showActiveOnly ? isActive : !isActive;
    });

    //----------------------html return------------------------
    return (
        <div className="desktop-1">
            <Header />
            <main className="main-content">
                <button className="back-btn" onClick={() => navigate("/dashboard")}>
                    ← Back to Dashboard
                </button>

                <h1 className="page-title">All Polls</h1>

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
                    filteredPolls.map(poll => (
                        <VotingCard key={poll.id} poll={poll} />
                    ))
                ) : (
                    <p>No {showActiveOnly ? "active" : "expired"} polls available.</p>
                )}
            </main>
        </div>
    );
};

export default VotingPage;