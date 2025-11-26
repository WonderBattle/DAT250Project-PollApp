import axios from "../axiosConfig";

const API_URL = "/auth";

/**
 * Registers a new user.
 *
 * @param {string} username - The username of the new user.
 * @param {string} email - The email of the new user.
 * @param {string} password - The password of the new user.
 * @returns {Promise} Axios POST request Promise containing the created user data.
 */
export const register = (username, email, password) => {
    return axios.post(`${API_URL}/register`, { username, email, password });
};

/**
 * Logs in an existing user.
 *
 * @param {string} email - The email of the user.
 * @param {string} password - The password of the user.
 * @returns {Promise} Axios POST request Promise containing JWT token and user info.
 */
export const login = (email, password) => {
    return axios.post(`${API_URL}/login`, { email, password });
};