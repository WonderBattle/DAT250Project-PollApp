import React from "react";
import "../styles/Footer.css";

/**
 * Footer component displayed at the bottom of the application.
 * Shows a decorative rectangle and copyright information.
 *
 * @returns {JSX.Element} The rendered Footer component
 */
const Footer = () => {
    return (
        <div>
            {/* Decorative rectangle */}
            <div className="rectangle-1"></div>

            {/* Copyright text */}
            <div className="_2025-poll-app">© 2025 Poll App</div>
        </div>
    );
};

export default Footer;