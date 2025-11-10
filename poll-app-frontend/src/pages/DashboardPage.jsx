import React, { useEffect, useState } from "react";
import PollCard from "../components/PollCard";
import Header from "../components/Header";
import CreatePollCard from "../components/CreatePollCard"
import "../styles/Dashboard.css";
import {deletePoll} from "../apiConfig/pollApi";
import { getAllPolls } from "../apiConfig/pollApi";


//------------------sample poll data just for visual testing------------------
/*const samplePolls = [
    {
        id: 1,
        question: "What’s your favorite pastel color?",
        createdBy: "Enikő",
        validUntil: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
        options: ["Pink", "Lavender", "Mint", "Peach"],
        totalVotes: 12,
    }
];*/


const Dashboard = () => {
    const [polls, setPolls] = useState([]);
    const [showCreatePollCard, setShowCreatePollCard] = useState(false);
   //saving current logged in user
    const [currentUser, setCurrentUser] = useState(null);

    useEffect(() => {
        const storedUser = localStorage.getItem("user");
        if (storedUser) {
            setCurrentUser(JSON.parse(storedUser));
        }
    }, []);

    useEffect(() => {
        const fetchPolls = async () => {
            try {
                const data = await getAllPolls();

                if (!data || data.length === 0) {
                    console.warn("No polls found: The database is empty.");
                } else {
                    console.log(`Successfully fetched ${data.length} polls from backend.`);
                }

                setPolls(data);
            } catch (error) {
                console.error("Error fetching polls from backend:", error.message || error);
            }
        };
        fetchPolls();
    }, []);

    //--------------------simulate loading polls--------------------
    /*useEffect(() => {
        setTimeout(() => {
            setPolls(samplePolls);
        }, 500);
    }, []);*/


    //-------------------delete poll -------------------
    const handleDeletePoll = async (pollId) => {
        try {
            await deletePoll(pollId);
            setPolls((prev) => prev.filter((p) => p.id !== pollId));
        } catch (error) {
            console.error("Failed to delete poll:", error);
        }
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