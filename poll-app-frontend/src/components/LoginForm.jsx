import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/LoginForm.css";

const LoginForm = ({ onSwitch }) => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const navigate = useNavigate();

    //------------------ Handle Login ------------------
    const handleLogin = () => {
        if (email.trim() === "" || password.trim() === "") {
            alert("Please enter both email and password");
            return;
        }

        // Simulate a login
        localStorage.setItem("user", JSON.stringify({ email }));
        console.log("Login successful:", email);
        navigate("/dashboard");
    };

    //------------------ HTML to visualize ------------------
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