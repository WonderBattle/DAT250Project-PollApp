import axios from "../axiosConfig";

const API_URL = "/auth"; // axios baseURL already set

export const register = (username, email, password) => {
    return axios.post(`${API_URL}/register`, { username, email, password });
};

export const login = (email, password) => {
    return axios.post(`${API_URL}/login`, { email, password });
};