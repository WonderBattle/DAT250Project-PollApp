import React, {useEffect, useState} from "react";
import "../styles/Header.css";
import {useNavigate} from "react-router-dom";

const Header = () => {
    const navigate = useNavigate();
    //const [isLoggedIn, setIsLoggedIn] = useState(true);
    const [isLoggedIn, setIsLoggedIn] = useState(false);

    /*
    useEffect(() => {
        const user = localStorage.getItem("user");
        setIsLoggedIn(!!user);
    }, []);
     */

    useEffect(() => {
        const token = localStorage.getItem("token"); // token is saved at login
        setIsLoggedIn(!!token);
    }, []);


    /*
    const handleLogout = () => {
        localStorage.removeItem("user")
        console.log("user logged out");
        setIsLoggedIn(false)
        navigate("/");
    };
     */

    const handleLogout = () => {
        localStorage.removeItem("user");
        localStorage.removeItem("token"); // also remove JWT token
        console.log("user logged out");
        setIsLoggedIn(false);
        navigate("/");
    };


    return (
        <div>
            <div className="header"></div>
            <div className="poll-app-header">Poll App</div>
            {isLoggedIn && (
            <button className="logout-button" onClick={handleLogout}> Logout</button>)}
        </div>
    );
};
export default Header;