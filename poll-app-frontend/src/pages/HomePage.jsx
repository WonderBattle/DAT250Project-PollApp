import React, {useState} from "react";
import LoginForm from "../components/LoginForm";
import "../styles/HomePage.css"
import Header from "../components/Header";
import Footer from "../components/Footer";
import RegisterForm from "../components/RegisterForm";

const HomePage = () => {
    const [showRegister, setShowregister] = useState(false);
    return (
        <div className="desktop-1">
            <Header/>
            <main className="main-content">
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