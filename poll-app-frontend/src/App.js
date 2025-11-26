import React from "react";
import { Routes, Route } from "react-router-dom";
import HomePage from "./pages/HomePage";
import Dashboard from "./pages/DashboardPage";
import VotingPage from "./pages/VotingPage";
import PublicDashboardPage from "./pages/PublicDashboardPage";
import PublicVotingPage from "./pages/PublicVotingPage";

/**
 * App component defines all the frontend routes for the Poll App.
 *
 * Routes:
 *  "/"                  - Home page (login/register)
 *  "/dashboard"         - User's dashboard for managing polls
 *  "/votingpage"        - Page showing all polls for voting
 *  "/publicdashboard"   - Public polls dashboard
 *  "/vote/public/:pollId" - Public voting page for a specific poll
 *
 * @component
 * @returns {JSX.Element} The root component with defined routes.
 */
function App() {
    return (
        <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/dashboard" element={<Dashboard/>} />
            <Route path="/votingpage" element={<VotingPage />} />
            <Route path="/publicdashboard" element={<PublicDashboardPage />} />
            <Route path="/vote/public/:pollId" element={<PublicVotingPage/>}/>
        </Routes>
    );
}

export default App;