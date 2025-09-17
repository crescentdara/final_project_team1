import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8080/web/api",
});

export const getTest = async () => {
    const res = await api.get("/test");
    return res.data;
};
