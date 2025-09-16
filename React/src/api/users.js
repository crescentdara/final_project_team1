import axios from "axios";

// 서버에 context-path가 있으면 '/land-survey/api' 로 바꾸세요.
const api = axios.create({ baseURL: "/api", timeout: 10000 });

export async function fetchUsers(keyword = '') {
  const res = await api.get('/users', { params: { keyword } })
  return Array.isArray(res.data) ? res.data : []
}

export async function fetchUserDetail(userId) {
  const res = await api.get(`/users/${userId}`);
  return res.data; // {userId, username, name, role, status, createdAt}
}

export async function fetchUserAssignments(userId) {
  const res = await api.get(`/users/${userId}/assignments`);
  return Array.isArray(res.data) ? res.data : []; // [{buildingId, lotAddress}]
}
