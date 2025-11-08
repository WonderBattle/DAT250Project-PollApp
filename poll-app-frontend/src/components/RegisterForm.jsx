import React, {useState} from "react";
import "../styles/LoginForm.css";
import { register } from "../apiConfig/authApi";

//-----------------------constant variables----------------------------

const RegisterForm = ({onSwitch}) => {
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

//-----------------------registrating new (fake) user----------------------------
    const handleRegister = async () => {
        if (!username || !email || !password) {
            alert("Please fill in all fields");
            return;
        }

        /*
        try {
            // Fake register for now
            const newUser = { username, email };
            localStorage.setItem("user", JSON.stringify(newUser));
            console.log("Registered user:", newUser);
            alert("Registration successful!");
            onSwitch(); // switching back here to login form
        } catch (error) {
            console.error("Registration error:", error);
            alert("Something went wrong");
        }
         */

        try {
            // Call backend register endpoint
            await register(username, email, password);
            console.log("Registered user:", { username, email });
            alert("Registration successful! Please log in.");
            onSwitch(); // switch back to login form
        } catch (error) {
            console.error("Registration error:", error);
            // If server returns a message, show it; otherwise generic message
            const msg = error?.response?.data || "Something went wrong";
            alert(`Registration failed: ${msg}`);
        }

    };

    //-----------------------chtml returning----------------------------

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