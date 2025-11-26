import axiosConfig from "../axiosConfig";

/**
 * Fetches all polls.
 * @returns {Promise<Array>} Array of poll objects.
 */
export const getAllPolls = async () => {
    const response = await axiosConfig.get("/polls");
    return response.data;
};

/**
 * Creates a new poll.
 * @param {Object} payload - Poll data to create.
 * @returns {Promise<Object>} Created poll object.
 */
export const createPoll = async (payload) => {
    const response = await axiosConfig.post("/polls", payload);
    return response.data;
};

/**
 * Fetches a poll by its ID.
 * @param {string} pollId - The ID of the poll.
 * @returns {Promise<Object>} Poll object.
 */
export const getPollById = async (pollId) => {
    const response = await axiosConfig.get(`/polls/${pollId}`);
    return response.data;
};

/**
 * Deletes a poll by ID.
 * @param {string} pollId - The ID of the poll to delete.
 * @returns {Promise<void>}
 */
export const deletePoll = async (pollId) => {
    await axiosConfig.delete(`/polls/${pollId}`);
};

/**
 * Adds an option to a poll.
 * @param {string} pollId - The ID of the poll.
 * @param {Object} optionData - Option data (e.g., caption, presentationOrder).
 * @returns {Promise<Object>} Created option object.
 */
export const addOption = async (pollId, optionData) => {
    const response = await axiosConfig.post(`/polls/${pollId}/options`, optionData);
    return response.data;
};

/**
 * Deletes an option from a poll.
 * @param {string} pollId - The ID of the poll.
 * @param {string} optionId - The ID of the option to delete.
 * @returns {Promise<void>}
 */
export const deleteOption = async (pollId, optionId) => {
    await axiosConfig.delete(`/polls/${pollId}/options/${optionId}`);
};

/**
 * Fetches a private poll by ID.
 * @param {string} pollId - The ID of the private poll.
 * @returns {Promise<Object>} Poll object.
 */
export const getPrivatePollById = async (pollId) => {
    const response = await axiosConfig.get(`/polls/private/${pollId}`);
    return response.data;
};

/**
 * Creates a vote for a poll option.
 * @param {string} pollId - Poll ID.
 * @param {Object} voteData - Vote data containing voterId and optionId.
 * @returns {Promise<Object>} Created vote object.
 */
export const createVoteApi = async (pollId, voteData) => {
    const response = await axiosConfig.post(`/polls/${pollId}/votes`, voteData);
    return response.data;
};

/**
 * Fetches poll results (vote counts for each option).
 * @param {string} pollId - Poll ID.
 * @returns {Promise<Object>} Mapping of optionId to voteCount.
 */
export const getPollResults = async (pollId) => {
    const response = await axiosConfig.get(`/polls/${pollId}/results`);
    return response.data;
};

/**
 * Fetches all public polls.
 * @returns {Promise<Array>} Array of public poll objects.
 */
export const getAllPublicPolls = async () => {
    const response = await axiosConfig.get("/polls/public");
    return response.data;
};

/**
 * Fetches polls created by a specific user.
 * @param {string} userId - User ID.
 * @returns {Promise<Array>} Array of polls by the user.
 */
export const usersPoll = async (userId) => {
    const response = await axiosConfig.get(`/polls/user/${userId}`);
    return response.data;
};

/**
 * Updates the visibility of a poll (public/private).
 * @param {string} pollId - Poll ID.
 * @param {boolean} isPublic - True for public, false for private.
 * @param {string} userId - ID of the user updating the poll.
 * @returns {Promise<Object>} Updated poll object.
 */
export const updatePollPrivacy = async (pollId, isPublic, userId) => {
    const response = await axiosConfig.put(
        `/polls/${pollId}/privacy`,
        {},
        { params: { isPublic, userId } }
    );
    return response.data;
};

/**
 * Updates an existing vote for a poll option.
 * @param {string} pollId - Poll ID.
 * @param {Object} voteData - Vote data containing voterId and optionId.
 * @returns {Promise<Object>} Updated vote object.
 */
export const updateVoteApi = async (pollId, voteData) => {
    const response = await axiosConfig.put(`/polls/${pollId}/votes`, voteData);
    return response.data;
};