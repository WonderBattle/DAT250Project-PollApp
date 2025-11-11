import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/LoginForm.css";
import { login } from "../apiConfig/authApi";

const LoginForm = ({ onSwitch }) => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const navigate = useNavigate();

    //------------------ Handle Login ------------------
    /*
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
     */

    const handleLogin = async () => {
        if (email.trim() === "" || password.trim() === "") {
            alert("Please enter both email and password");
            return;
        }

        try {
            // Call the backend login endpoint (AuthController /auth/login)
            const response = await login(email, password); // from authApi.js
            const { token, user } = response.data;

            // Save the token and user info locally
            localStorage.setItem("token", token);
            localStorage.setItem("user", JSON.stringify(user));

            console.log("Login successful:", user);
            navigate("/dashboard");
        } catch (err) {
            console.error("Login failed", err);
            alert("Invalid credentials or server error");
        }
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