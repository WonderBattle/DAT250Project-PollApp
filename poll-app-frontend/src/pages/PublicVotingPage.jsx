import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Header from "../components/Header";
import PublicVotingCard from "../components/PublicVotingCard";
import "../styles/VotingPage.css";
import { getPollById } from "../apiConfig/pollApi";

const PublicVotingPage = () => {
    const { pollId } = useParams();
    const [poll, setPoll] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchPoll = async () => {
            try {
                const data = await getPollById(pollId);
                setPoll(data);
            } catch (error) {
                console.error("Error fetching poll:", error);
            }
        };
        fetchPoll();
    }, [pollId]);

    return (
        <div className="desktop-1">
            <Header />

            <main className="main-content">
                <button className="back-btn" onClick={() => navigate("/publicdashboard")}>
                    ← Back to Public Polls
                </button>

                {poll ? (
                    <PublicVotingCard poll={poll} />
                ) : (
                    <p>Loading poll...</p>
                )}
            </main>
        </div>
    );
};

export default PublicVotingPage;