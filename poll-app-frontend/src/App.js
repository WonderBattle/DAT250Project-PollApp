import React from "react";
import { Routes, Route } from "react-router-dom";
import HomePage from "./pages/HomePage";
import Dashboard from "./pages/DashboardPage";
import VotingPage from "./pages/VotingPage";
import PublicDashboardPage from "./pages/PublicDashboardPage";
import PublicVotingPage from "./pages/PublicVotingPage";

function App() {
    return (
        <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/dashboard" element={<Dashboard/>} />
            <Route path="/vote/:pollId" element={<VotingPage />} />
            <Route path="/publicdashboard" element={<PublicDashboardPage />} />
            <Route path="/vote/public/:pollId" element={<PublicVotingPage/>}/>
        </Routes>
    );
}

export default App;