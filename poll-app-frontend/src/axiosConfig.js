import axios from "axios";

const axiosConfig = axios.create({
    // here we should put the Spring Boot backend, but I assuming it is this (if not we'll replace it)
    baseURL: "http://localhost:8080",
    headers: {
        "Content-Type": "application/json",
    },
    withCredentials:false,
});

export default axiosConfig;