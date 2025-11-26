import axiosConfig from "../axiosConfig";

/**
 * Creates a new user.
 * @param {Object} userData - User data (e.g., username, email, password).
 * @returns {Promise<Object>} Created user object.
 */
export const createUser = async (userData) => {
    const response = await  axiosConfig.post("/users", userData);
    return response.data;
};

/**
 * Fetches all users.
 * @returns {Promise<Array>} Array of user objects.
 */
export const getAllUsers = async () => {
    const response = await axiosConfig.get("/users");
    return response.data;
};

/**
 * Fetches a user by ID.
 * @param {string} id - User ID.
 * @returns {Promise<Object>} User object.
 */
export const getUserById = async (id) => {
    const response = await axiosConfig.get(`/users/${id}`);
    return response.data;
};