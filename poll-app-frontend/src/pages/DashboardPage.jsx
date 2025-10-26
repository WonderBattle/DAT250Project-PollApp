import React, { useEffect, useState } from "react";
import PollCard from "../components/PollCard";
import Header from "../components/Header";
import "../styles/Dashboard.css";
//import { getAllPolls } from "../apiConfig/pollApi";


//------------------sample poll data just for visual testing------------------
const samplePolls = [
    {
        id: 1,
        question: "What’s your favorite pastel color?",
        createdBy: "Enikő",
        validUntil: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
        options: ["Pink", "Lavender", "Mint", "Peach"],
        totalVotes: 12,
    }
];


const Dashboard = () => {
    const [polls, setPolls] = useState([]);

    //-------------fetch polls from backend ----------------
   /* useEffect(() => {
        const fetchPolls = async () => {
            try {
                const data = await getAllPolls();
                setPolls(data);
            } catch (error) {
                console.log("failed to fetch polls:", error);
            }
        };
        fetchPolls();
    }, []);*/

    //--------------------simulate loading polls--------------------
    useEffect(() => {
        setTimeout(() => {
            setPolls(samplePolls);
        }, 500);
    }, []);


    //-------------------delete poll -------------------
    const handleDeletePoll = (pollId) => {
        setPolls(polls.filter((p) => p.id !== pollId));
        console.log("Deleted poll with id:", pollId);
    };

    //-------------------vote click (redirect later) -------------------
    const handleVoteClick = (pollId) => {
        console.log("Redirect to vote page for poll:", pollId);
        // later we should use navigate(`/poll/${pollId}`);
    };

    //-------------------html return------------------------------
    return (
        <div className="desktop-1">
            <Header />
            <main className="main-content">
                <h1 className="page-title">Poll Dashboard</h1>
                {polls.length > 0 ? (
                    polls.map((poll) => (
                        <PollCard
                            key={poll.id}
                            poll={poll}
                            onDelete={handleDeletePoll}
                            onVote={handleVoteClick}
                        />
                    ))
                ) : (
                    <p>Loading polls...</p>
                )}
            </main>
        </div>
    );
};

export default Dashboard;