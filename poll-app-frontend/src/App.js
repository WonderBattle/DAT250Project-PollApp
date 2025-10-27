import React from "react";
import { Routes, Route } from "react-router-dom";
import HomePage from "./pages/HomePage";
import Dashboard from "./pages/DashboardPage";
import VotingPage from "./pages/VotingPage";

function App() {
    return (
        <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/dashboard" element={<Dashboard/>} />
            <Route path="/vote/:pollId" element={<VotingPage />} />
        </Routes>
    );
}

export default App;