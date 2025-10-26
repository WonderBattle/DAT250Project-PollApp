import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import Header from "../components/Header";
import VotingCard from "../components/VotingCard";
import "../styles/VotingPage.css";

//----------------sample data just for testing-----------------------
const samplePoll = {
    id: 1,
    question: "What’s your favorite pastel color?",
    options: ["Pink", "Lavender", "Mint", "Peach"],
    createdBy: "Enikő",
    publishedAt: new Date(),
    validUntil: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
};

const VotingPage = () => {
    const navigate = useNavigate();
    const { pollId } = useParams();
    const poll = samplePoll; //should be replaced with backend fetching

    //----------------------html return------------------------
    return (
        <div className="desktop-1">
            <Header />
            <main className="main-content">
                <button className="back-btn" onClick={() => navigate("/dashboard")}>
                    ← Back to Dashboard
                </button>
                <VotingCard poll={poll} />
            </main>
        </div>
    );
};

export default VotingPage;