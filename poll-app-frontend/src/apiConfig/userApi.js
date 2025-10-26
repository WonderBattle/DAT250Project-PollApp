import axiosConfig from "../axiosConfig";

//---------------creating a new user------------------------------
export const createUser = async (userData) => {
    const response = await  axiosConfig.post("/users", userData);
    return response.data;
};

//-----------------getting all the users---------------------------
export const getAllUsers = async  () => {
    const response = await  axiosConfig.get("/users");
    return response.data;
};

//---------------------getting a user by ID ---------------------
export const getUserById = async  (id) => {
    const response = await axiosConfig.get(`/users/${id}`);
    return response.data;
};


