import React, { useEffect, useState } from "react";
import PollCard from "../components/PollCard";
import Header from "../components/Header";
import CreatePollCard from "../components/CreatePollCard"
import "../styles/Dashboard.css";
import {deletePoll, getPrivatePollById} from "../apiConfig/pollApi";


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
    const [showActiveOnly, setShowActiveOnly] = useState(true);

    useEffect(() => {
        const storedUser = localStorage.getItem("user");
        if (storedUser) {
            setCurrentUser(JSON.parse(storedUser));
        }
    }, []);

    useEffect(() => {
        const fetchPolls = async () => {
            if (!currentUser) return; // Wait until user is loaded

            try {
                const data = await getPrivatePollById(currentUser.id);

                if (!data || data.length === 0) {
                    console.warn("No polls found for this user.");
                } else {
                    console.log(`Fetched ${data.length} private polls for user ${currentUser.username}`);
                }

                setPolls(data);
            } catch (error) {
                console.error("Error fetching polls from backend:", error.message || error);
            }
        };
        fetchPolls();
    }, [currentUser]);

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
    //-------------------toggle switch for active or expired listing -------------------
    const filteredPolls = polls.filter((poll) => {
        const now = new Date();
        const validUntil = new Date(poll.validUntil);
        const isActive = validUntil > now;
        return showActiveOnly ? isActive : !isActive;
    });
    //-------------------html return------------------------------
    return (
        <div className="desktop-1">
            <Header />
            <main className="main-content">
                <h1 className="page-title">Poll Dashboard</h1>

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

export default Dashboard;