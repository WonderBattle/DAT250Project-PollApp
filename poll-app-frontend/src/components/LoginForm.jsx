import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/LoginForm.css";
import { login } from "../apiConfig/authApi";

/**
 * LoginForm component allows users to log in with email and password.
 *
 * @component
 * @param {Object} props - Component props
 * @param {Function} props.onSwitch - Function to switch between login and register forms
 * @returns {JSX.Element} The rendered LoginForm component
 */
const LoginForm = ({ onSwitch }) => {
    /** @type {[string, Function]} State for user email input */
    const [email, setEmail] = useState("");

    /** @type {[string, Function]} State for user password input */
    const [password, setPassword] = useState("");

    const navigate = useNavigate();

    /**
     * Handles the login process.
     * Validates input, calls the backend login endpoint, and stores the token and user info in localStorage.
     * Navigates to the dashboard on success.
     */
    const handleLogin = async () => {
        if (email.trim() === "" || password.trim() === "") {
            alert("Please enter both email and password");
            return;
        }

        try {
            const response = await login(email, password);
            const { token, user } = response.data;

            localStorage.setItem("token", token);
            localStorage.setItem("user", JSON.stringify(user));

            console.log("Login successful:", user);
            navigate("/dashboard");
        } catch (err) {
            console.error("Login failed", err);
            alert("Invalid credentials or server error");
        }
    };

    return (
        <div className="login-card">
            <h2 className="welcome">Welcome Back</h2>
            <p className="login-to-vote-or-create-polls">
                Login to vote or create polls
            </p>

            <input
                type="email"
                placeholder="Email"
                className="username-text-box"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
            />

            <input
                type="password"
                placeholder="Password"
                className="password-text-box"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
            />

            <button className="log-in-button" onClick={handleLogin}>
                <span className="log-in-button-label">Login</span>
            </button>

            <p className="no-account-text">Don’t have an account?</p>
            <button
                className="register-button"
                onClick={() => onSwitch("register")}
            >
                Register
            </button>
        </div>
    );
};

export default LoginForm;