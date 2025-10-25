import React from "react";
import LoginForm from "../components/LoginForm";
import "../styles/HomePage.css"
import Header from "./Header";
import Footer from "./Footer";

const HomePage = () => {
    return (
        <div className="desktop-1">
            <Header/>
            <LoginForm/>
            <Footer/>
        </div>
    );
};

export default HomePage;