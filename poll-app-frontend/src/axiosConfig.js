import axios from "axios";

const axiosConfig = axios.create({
    // here we should put the Spring Boot backend, but I assuming it is this (if not we'll replace it)
    baseURL: "http://localhost:8080",
    headers: {
        "Content-Type": "application/json",
    },
    withCredentials:false,
});

// Automatically add Authorization header with JWT from localStorage (if exists)
axiosConfig.interceptors.request.use((config) => {
    const token = localStorage.getItem("token"); // read token saved on login
    if (token) {
        config.headers.Authorization = `Bearer ${token}`; // add it to every request
    }
    return config;
});



export default axiosConfig;