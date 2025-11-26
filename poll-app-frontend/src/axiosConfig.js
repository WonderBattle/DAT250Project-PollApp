import axios from "axios";

const axiosConfig = axios.create({
    baseURL: "http://localhost:8080",
    headers: {
        "Content-Type": "application/json",
    },
    withCredentials:false,
});

axiosConfig.interceptors.request.use((config) => {
    const token = localStorage.getItem("token"); // read token saved on login
    if (token) {
        config.headers.Authorization = `Bearer ${token}`; // add it to every request
    }
    return config;
});

export default axiosConfig;