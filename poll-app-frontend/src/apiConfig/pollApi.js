import axiosConfig from "../axiosConfig";

//--------------getting all polls-----------
export const getAllPolls = async () => {
    const response = await axiosConfig.get("/polls");
    return response.data;
};

//--------------creating a new poll ------------
export const  createPoll = async (pollDate) => {
    const  response = await axiosConfig.post("/polls", pollDate);
    return response.data;
};

//----------getting poll by ID---------------
export  const  getPollById = async (pollId) => {
    const  response = await  axiosConfig.get(`/polls/${pollId}`);
    return response.data;
};

//----------------deleting a poll----------------------
export const deletePoll = async (pollId) => {
    await  axiosConfig(`/polls/${pollId}`);
};

export const addOption = async (pollId, optionData) => {
    const response = await axiosConfig.post(`/polls/${pollId}/options`, optionData);
    return response.data;
};