import axiosConfig from "../axiosConfig";

//--------------getting all polls-----------
export const getAllPolls = async () => {
    const response = await axiosConfig.get("/polls");
    return response.data;
};

//--------------creating a new poll ------------
export const createPoll = async (payload, token) => {
    const response = await axiosConfig.post("/polls", payload, {
        headers: {
            Authorization: token ? `Bearer ${token}` : undefined
        }
    });
    return response.data;
};

//----------getting poll by ID---------------
export  const  getPollById = async (pollId) => {
    const  response = await  axiosConfig.get(`/polls/${pollId}`);
    return response.data;
};

//----------------deleting a poll----------------------
export const deletePoll = async (pollId) => {
    await  axiosConfig.delete(`/polls/${pollId}`);
};

//----------------add option----------------------
export const addOption = async (pollId, optionData) => {
    const response = await axiosConfig.post(`/polls/${pollId}/options`, optionData);
    return response.data;
};

//----------------delete option----------------------
export const deleteOption = async (pollId, optionId) => {
    await axiosConfig.delete(`/polls/${pollId}/options/${optionId}`);
};

//----------getting poll by ID (only private)---------------
export  const  getPrivatePollById = async (pollId) => {
    const  response = await  axiosConfig.get(`/polls/private/${pollId}`);
    return response.data;
};


// ------------------ create a vote ------------------
export const createVoteApi = async (pollId, voteData) => {
    const response = await axiosConfig.post(`/polls/${pollId}/votes`, voteData);
    return response.data;
};

// ------------------ get poll results ------------------
export const getPollResults = async (pollId) => {
    const response = await axiosConfig.get(`/polls/${pollId}/results`);
    return response.data; // returns { optionId: voteCount }
};

export const getAllPublicPolls = async () => {
    const response = await axiosConfig.get("/polls/public");
    return response.data;
};

export const usersPoll = async () => {
    const  response = await axiosConfig.get(`polls/user/{userID}`);
    return response.data;
}