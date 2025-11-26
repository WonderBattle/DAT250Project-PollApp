import React, { useEffect, useState } from "react";
import "../styles/Header.css";
import { useNavigate } from "react-router-dom";

/**
 * Header component displayed at the top of the app.
 * Shows the app title and a logout button if the user is logged in.
 *
 * @component
 * @returns {JSX.Element} The rendered Header component
 */
const Header = () => {
    const navigate = useNavigate();

    /** @type {[boolean, Function]} State to track if a user is logged in */
    const [isLoggedIn, setIsLoggedIn] = useState(false);

    /**
     * Check for a token in localStorage on component mount to determine login status.
     */
    useEffect(() => {
        const token = localStorage.getItem("token");
        setIsLoggedIn(!!token);
    }, []);

    /**
     * Handle logout action.
     * Clears user information and token from localStorage and navigates to home.
     */
    const handleLogout = () => {
        localStorage.removeItem("user");
        localStorage.removeItem("token");
        console.log("user logged out");
        setIsLoggedIn(false);
        navigate("/");
    };

    return (
        <div>
            <div className="header"></div>
            <div className="poll-app-header">Poll App</div>
            {isLoggedIn && (
                <button className="logout-button" onClick={handleLogout}>
                    Logout
                </button>
            )}
        </div>
    );
};

export default Header;