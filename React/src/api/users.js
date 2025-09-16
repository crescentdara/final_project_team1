import axios from "axios";


const api = axios.create({
  baseURL: "/api",
  timeout: 10000,
});

export async function fetchUsers(keyword = "") {
  const res = await api.get("/users", { params: { keyword } });
  return res.data;
}

export async function fetchUserDetail(userId) {
  const res = await api.get(`/users/${userId}`);
  return res.data;
}

export async function fetchUserAssignments(userId) {
  const res = await api.get(`/users/${userId}/assginments`);
  return res.data;
}