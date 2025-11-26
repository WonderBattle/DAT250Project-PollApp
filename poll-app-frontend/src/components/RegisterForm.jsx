import React, {useState} from "react";
import "../styles/LoginForm.css";
import { register } from "../apiConfig/authApi";

/**
 * RegisterForm component allows a new user to register.
 * Users can enter a username, email and password, then submit to register.
 *
 * @component
 * @param {Object} props - Component props
 * @param {function} props.onSwitch - Callback function to switch back to login form
 * @returns {JSX.Element} Rendered RegisterForm component
 */
const RegisterForm = ({ onSwitch }) => {
    /** Username input state */
    const [username, setUsername] = useState("");

    /** Email input state */
    const [email, setEmail] = useState("");

    /** Password input state */
    const [password, setPassword] = useState("");

    /**
     * Validates email format.
     * @param {string} email
     * @returns {boolean} True if valid, false otherwise
     */
    const isValidEmail = (email) => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    };

    /**
     * Validates password: minimum 6 characters, must include letters and numbers.
     * @param {string} password
     * @returns {boolean} True if valid, false otherwise
     */
    const isValidPassword = (password) => {
        const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,}$/;
        return passwordRegex.test(password);
    };

    /**
     * Handles the registration of a new user.
     * Calls backend API and saves the returned user object to localStorage.
     * Alerts the user about success or failure.
     */
    const handleRegister = async () => {
        if (!username || !email || !password) {
            alert("Please fill in all fields");
            return;
        }

        if (!isValidEmail(email)) {
            alert("Please enter a valid email address");
            return;
        }

        if (!isValidPassword(password)) {
            alert("Password must be at least 6 characters long and contain letters and numbers");
            return;
        }

        try {
            const response = await register(username, email, password);
            const createdUser = response.data;

            localStorage.setItem("user", JSON.stringify(createdUser));

            console.log("Registered user:", createdUser);
            alert("Registration successful! Please log in.");
            onSwitch();
        } catch (error) {
            console.error("Registration error:", error);
            const msg = error?.response?.data || "Something went wrong";
            alert(`Registration failed: ${msg}`);
        }
    };

    return (
        <div className="login-card">
            <h2 className="welcome">Create Account</h2>
            <p className="login-to-vote-or-create-polls">
                Register to start voting and creating polls
            </p>

            <input
                type="text"
                placeholder="Username"
                className="username-text-box"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
            />

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

            <button className="log-in-button" onClick={handleRegister}>
                <span className="log-in-button-label">Register</span>
            </button>

            <p className="no-account-text">Already have an account?</p>
            <button className="register-button" onClick={onSwitch}>
                Back to Login
            </button>
        </div>
    );
};

export  default  RegisterForm;