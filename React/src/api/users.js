import axios from "axios";

const api = axios.create({ baseURL: "/api", timeout: 10000 });

export async function fetchUsersAdvanced({ field = "all", keyword = "" } = {}) {
  // 백엔드 파라미터 매핑
  const backendField = field === "role" ? "role" : "name";
  const res = await api.get("/users", { params: { field: backendField, keyword } });

  const d = res?.data;
  const arr =
      Array.isArray(d) ? d :
          Array.isArray(d?.items) ? d.items :
              Array.isArray(d?.data) ? d.data :
                  Array.isArray(d?.users) ? d.users : [];

  const normalized = arr.map(u => ({
    userId: u.userId ?? u.user_id ?? u.id,
    username: u.username ?? u.user_name ?? "",
    name: u.name ?? "",
    role: typeof u.role === "string" ? u.role : (u.role?.name ?? ""),
    status: u.status,
    createdAt: u.createdAt,
  }));

  // 프론트 추가 필터 (username 전용 등)
  const kw = String(keyword ?? "").trim();
  if (!kw) return normalized;

  if (field === "username") {
    const low = kw.toLowerCase();
    return normalized.filter(u => (u.username ?? "").toLowerCase().includes(low));
  }

  if (field === "all") {
    const low = kw.toLowerCase();
    const up = kw.toUpperCase();
    return normalized.filter(u =>
        (u.name ?? "").toLowerCase().includes(low) ||
        (u.username ?? "").toLowerCase().includes(low) ||
        (u.role ?? "").toUpperCase() === up
    );
  }

  return normalized;
}

export async function fetchUserDetail(userId) {
  const res = await api.get(`/users/${userId}`);
  return res.data; // {userId, username, name, role, status, createdAt}
}

export async function fetchUserAssignments(userId) {
  const res = await api.get(`/users/${userId}/assignments`);
  return Array.isArray(res.data) ? res.data : []; // [{buildingId, lotAddress}]
}
