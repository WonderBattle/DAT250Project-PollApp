import React from "react";
import LoginForm from "../components/LoginForm";
import "../styles/HomePage.css"

const HomePage = () => {
    return (
        <div className="desktop-1">
            <div className="header"></div>
            <div className="poll-app-header">Poll App</div>
            <LoginForm/>
            <div className="rectangle-1"></div>
            <div className="_2025-poll-app">© 2025 Poll App</div>
        </div>
    );
};

export default HomePage;