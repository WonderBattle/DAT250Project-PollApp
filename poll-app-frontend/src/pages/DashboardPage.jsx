import React, {useEffect, useState} from "react";
import PollCard from "../components/PollCard";
import Header from "../components/Header";
import "../styles/Dashboard.css";
import {getAllPolls} from "../apiConfig/pollApi";

//---------------sample data for texting UI---------------
const samplePoll = {
    id: 1,
    question: "What’s your favorite pastel color?",
    options: ["Pink", "Lavender", "Mint", "Peach"],
    createdBy: "Enikő",
    createdAt: new Date(),
    totalVotes: 12,
};
const samplePoll2 = {
    id: 1,
    question: "What’s your favorite pastel color?",
    options: ["Pink", "Lavender", "Mint", "Peach"],
    createdBy: "Enikő",
    createdAt: new Date(),
    totalVotes: 12,
};
const samplePoll3 = {
    id: 1,
    question: "What’s your favorite pastel color?",
    options: ["Pink", "Lavender", "Mint", "Peach"],
    createdBy: "Enikő",
    createdAt: new Date(),
    totalVotes: 12,
};


const Dashboard = () => {
    const [polls, setPolls] = useState([]);
    //-------------method of get polls from backend ----------------
    useEffect(() => {
        const fetchPolls = async () => {
            try {
                const data = await getAllPolls();
                setPolls(data);
            } catch (error) {
                console.log("failed to fetch polls:", error);
            }
        };
        fetchPolls();
    }, []);

//---------------------html returning-------------------
    return (
        <div className="desktop-1">
            <Header />
            <main className="main-content">
                <h1 className="page-title">Poll Dashboard</h1>
                <PollCard poll={samplePoll} />
                <PollCard poll={samplePoll2} />
                <PollCard poll={samplePoll3} />
                {polls.length > 0 ? (
                    polls.map((poll) => <PollCard key={poll.id} poll={poll} />)
                ) : (
                    <p>Loading polls...</p>
                )}
            </main>
        </div>
    );
};

export default Dashboard;
