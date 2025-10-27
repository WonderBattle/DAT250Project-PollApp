import React, {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import Header from "../components/Header";
import VotingCard from "../components/VotingCard";
import "../styles/VotingPage.css";
import {getPollById} from "../apiConfig/pollApi";

//----------------sample data just for testing-----------------------
/*const samplePoll = {
    id: 1,
    question: "What’s your favorite pastel color?",
    options: ["Pink", "Lavender", "Mint", "Peach"],
    createdBy: "Enikő",
    publishedAt: new Date(),
    validUntil: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
};*/

const VotingPage = () => {
    const { pollId } = useParams();
    const [poll, setPoll] = useState(null);
    const navigate = useNavigate();
    // const poll = samplePoll; //should be replaced with backend fetching

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

    //----------------------html return------------------------
    return (
        <div className="desktop-1">
            <Header />
            <main className="main-content">
                <button className="back-btn" onClick={() => navigate("/dashboard")}>
                    ← Back to Dashboard
                </button>
                {poll ? <VotingCard poll={poll} /> : <p>Loading poll...</p>}
            </main>
        </div>
    );
};

export default VotingPage;