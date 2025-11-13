import React, {useState} from "react";
import LoginForm from "../components/LoginForm";
import "../styles/HomePage.css"
import Header from "../components/Header";
import Footer from "../components/Footer";
import RegisterForm from "../components/RegisterForm";
import {useNavigate} from "react-router-dom";

const HomePage = () => {
    const [showRegister, setShowregister] = useState(false);
    const navigate = useNavigate()
    return (
        <div className="desktop-1">
            <Header/>
            <main className="main-content">
                <div className="public-polls-container">
                    <button
                        className="public-polls-btn"
                        onClick={() => navigate("/publicdashboard")}
                    >
                        Public Polls
                    </button>
                </div>
                {showRegister ? (
                    <RegisterForm onSwitch={() => setShowregister(false)} />
                ) : (
                    <LoginForm onSwitch={() => setShowregister(true)} />
                )}
            </main>
            <Footer/>
        </div>
    );
};

export default HomePage;