import React, {useState} from "react";
import {useNavigate} from "react-router-dom";
import "../styles/LoginForm.css";

//-----------------------constant variables----------------------------
const LoginForm = () => {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const navigate = useNavigate();

//------------------methods handling actions/logic-----------------------
    const handleLogin = () => {
        if (username.trim() === "") {
            alert("please enter a username");
            return;
        }
        //since we do not have backend, I'll just simulate a login here
        localStorage.setItem("user", username);
        console.log("login attempt: ", username, password)
        navigate("/dashboard");
    };

//-----------------HTML to visualize-------------------------------
    return (
            <div className="login-card">
                <h2 className="welcome">Welcome</h2>
                <p className="login-to-vote-or-create-polls">
                    Login to vote or create polls
                </p>

                <input
                    type="text"
                    placeholder="Username"
                    className="username-text-box"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
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
            </div>
    );
};
export default LoginForm;