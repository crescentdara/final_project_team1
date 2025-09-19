import axios from "axios";

const api = axios.create({ baseURL: "/web/api", timeout: 10000 });

// ✅ 페이징 + 검색 포함된 조사원 리스트
export async function fetchUsersPaged({ page = 0, size = 10, field = "all", keyword = "" } = {}) {
    const res = await api.get("/users/page", {
        params: { page, size, field, keyword }
    });

    // 서버에서 Page<UserSimpleDto> 구조 반환
    // { content, totalPages, totalElements, size, number, first, last ... }
    return res.data;
}

// ✅ 단건 상세 조회
export async function fetchUserDetail(userId) {
    const res = await api.get(`/users/${userId}`);
    return res.data;
}

// ✅ 배정 건물 조회
export async function fetchUserAssignments(userId) {
    const res = await api.get(`/users/${userId}/assignments`);
    return Array.isArray(res.data) ? res.data : [];
}

// ✅ 간단 조사원 리스트 (초기 로딩 등)
export async function fetchUsersSimple() {
    const res = await api.get("/users/simple");
    const arr = Array.isArray(res.data) ? res.data : [];

    return arr.map(u => ({
        userId: u.userId,
        username: u.username,
        name: u.name,
        empNo: u.empNo,
        role: u.role,
        status: u.status,
    }));
}
