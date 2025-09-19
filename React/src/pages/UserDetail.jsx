import { useEffect, useState } from "react";
import { fetchUsersPaged, fetchUserDetail, fetchUserAssignments } from "../api/users";
import UserDetailModal from "../components/users/UserDetailModal";

export default function UserDetail() {
    const [users, setUsers] = useState([]);
    const [loadingUsers, setLoadingUsers] = useState(false);
    const [error, setError] = useState(null);

    const [search, setSearch] = useState("");
    const [field, setField] = useState("all");

    const [page, setPage] = useState(0);   // 현재 페이지 (0부터 시작)
    const [totalPages, setTotalPages] = useState(0);
    const pageSize = 10;

    const [selectedUserId, setSelectedUserId] = useState(null);
    const [detail, setDetail] = useState(null);
    const [assignments, setAssignments] = useState([]);
    const [loadingDetail, setLoadingDetail] = useState(false);
    const [loadingAssign, setLoadingAssign] = useState(false);

    // ✅ 사용자 목록 로드
    useEffect(() => {
        loadUsers(page, search, field);
    }, [page]);

    async function loadUsers(page = 0, keyword = "", field = "all") {
        setLoadingUsers(true); setError(null);
        try {
            const data = await fetchUsersPaged({ page, size: pageSize, field, keyword });
            setUsers(data.content);
            setTotalPages(data.totalPages);
        } catch (e) {
            setError(e);
        } finally {
            setLoadingUsers(false);
        }
    }

    // ✅ 검색 버튼
    const handleSearch = () => {
        setPage(0); // 검색하면 첫 페이지로
        loadUsers(0, search.trim(), field);
    };

    // ✅ 상세/배정 로드
    useEffect(() => {
        if (!selectedUserId) { setDetail(null); setAssignments([]); return; }

        let alive = true;
        (async () => {
            try {
                setLoadingDetail(true); setLoadingAssign(true);
                const d = await fetchUserDetail(selectedUserId);
                if (alive) setDetail(d);
                const list = await fetchUserAssignments(selectedUserId);
                if (alive) setAssignments(list);
            } catch (e) {
                if (alive) setError(e);
            } finally {
                if (alive) { setLoadingDetail(false); setLoadingAssign(false); }
            }
        })();

        return () => { alive = false; };
    }, [selectedUserId]);

    // ✅ 페이지네이션 UI
    const renderPagination = () => (
        <nav className="mt-3">
            <ul className="pagination justify-content-center">
                <li className={`page-item ${page === 0 ? "disabled" : ""}`}>
                    <button className="page-link" onClick={() => setPage(p => p - 1)}>이전</button>
                </li>
                {Array.from({ length: totalPages }, (_, i) => (
                    <li key={i} className={`page-item ${page === i ? "active" : ""}`}>
                        <button className="page-link" onClick={() => setPage(i)}>{i + 1}</button>
                    </li>
                ))}
                <li className={`page-item ${page === totalPages - 1 ? "disabled" : ""}`}>
                    <button className="page-link" onClick={() => setPage(p => p + 1)}>다음</button>
                </li>
            </ul>
        </nav>
    );

    return (
        <div className="container mt-4">
            <h3 className="mb-3">조사원 목록</h3>

            {/* 🔍 검색 영역 */}
            <div className="mb-4">
                <div className="input-group w-100">
                    <select
                        className="form-select"
                        style={{ maxWidth: "120px" }}
                        value={field}
                        onChange={(e) => setField(e.target.value)}
                    >
                        <option value="all">전체</option>
                        <option value="name">이름</option>
                        <option value="username">아이디</option>
                        <option value="empNo">사번</option>
                    </select>
                    <input
                        type="text"
                        className="form-control"
                        placeholder="검색어 입력"
                        value={search}
                        onChange={e => setSearch(e.target.value)}
                    />
                    <button className="btn btn-primary" onClick={handleSearch}>검색</button>
                </div>
            </div>

            {error && (
                <div className="alert alert-danger mt-3">
                    데이터를 불러오지 못했습니다. {String(error.message || error)}
                </div>
            )}

            {loadingUsers ? (
                <p>로딩 중...</p>
            ) : users.length === 0 ? (
                <div className="text-center text-muted mt-4">검색 결과가 없습니다.</div>
            ) : (
                <div>
                    {users.map(u => (
                        <div
                            key={u.userId}
                            className="card mb-3 shadow-sm list-group-item-action"
                            style={{ cursor: "pointer", transition: "0.2s" }}
                            onClick={() => setSelectedUserId(u.userId)}
                        >
                            <div className="card-body d-flex justify-content-between align-items-center">
                                <div>
                                    <h6 className="mb-1">{u.name}</h6>
                                    <small className="text-muted">
                                        ID: {u.username} | 사번: {u.empNo}
                                    </small>
                                </div>
                                <button
                                    className="btn btn-outline-primary btn-sm"
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        setSelectedUserId(u.userId);
                                    }}
                                >
                                    상세보기
                                </button>
                            </div>
                        </div>
                    ))}
                    {renderPagination()}
                </div>
            )}

            <UserDetailModal
                show={!!selectedUserId}
                onHide={() => setSelectedUserId(null)}
                detail={detail}
                assignments={assignments}
                loadingDetail={loadingDetail}
                loadingAssign={loadingAssign}
            />
        </div>
    );
}
