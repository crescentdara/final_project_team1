import { useEffect, useState } from "react";
import { fetchUsersPaged, fetchUserDetail, fetchUserAssignments } from "../api/users";
import UserDetailPanel from "../components/users/UserDetailPanel.jsx";

export default function UserDetail() {
    const [users, setUsers] = useState([]);
    const [loadingUsers, setLoadingUsers] = useState(false);
    const [error, setError] = useState(null);

    const [search, setSearch] = useState("");
    const [field, setField] = useState("all");

    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const pageSize = 10;

    const [selectedUserId, setSelectedUserId] = useState(null);
    const [detail, setDetail] = useState(null);
    const [assignments, setAssignments] = useState([]);
    const [loadingDetail, setLoadingDetail] = useState(false);
    const [loadingAssign, setLoadingAssign] = useState(false);

    const [closing, setClosing] = useState(false);

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

    const handleSearch = () => {
        setPage(0);
        loadUsers(0, search.trim(), field);
    };

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

    const handleUserClick = (userId) => {
        if (selectedUserId === userId) {
            setClosing(true);
            setTimeout(() => {
                setSelectedUserId(null);
                setClosing(false);
            }, 300);
        } else {
            setSelectedUserId(userId);
            setClosing(false);
        }
    };

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
        <div
            className="container-fluid mt-4"
            style={{ display: "flex", gap: "20px", alignItems: "flex-start" }}
        >
            {/* 왼쪽: 조사원 목록 */}
            <div
                className="p-4 shadow-sm rounded-3 bg-white"
                style={{
                    flex: selectedUserId ? "0 0 60%" : "1 1 100%",
                    transition: "flex-basis 0.3s ease",
                }}
            >
                <h3
                    className="fw-bold mb-4"
                    style={{ borderLeft: "4px solid #6898FF", paddingLeft: "12px" }}
                >
                    조사원 목록
                </h3>

                {/* 🔍 검색 영역 */}
                <div className="mb-4 d-flex gap-2">
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
                        onChange={(e) => setSearch(e.target.value)}
                    />
                    <button className="btn btn-primary" onClick={handleSearch}>검색</button>
                </div>

                {/* 목록 */}
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
                                style={{
                                    cursor: "pointer",
                                    transition: "0.2s",
                                    border: selectedUserId === u.userId ? "2px solid #6898FF" : "1px solid #ddd",
                                }}
                                onClick={() => handleUserClick(u.userId)}
                            >
                                <div className="card-body d-flex justify-content-between align-items-center">
                                    <div>
                                        <h6 className="mb-1">{u.name}</h6>
                                        <small className="text-muted">
                                            ID: {u.username} | 사번: {u.empNo}
                                        </small>
                                    </div>
                                </div>
                            </div>
                        ))}
                        {renderPagination()}
                    </div>
                )}
            </div>

            {/* 오른쪽: 상세 패널 (그대로 유지) */}
            {selectedUserId && (
                <UserDetailPanel
                    isOpen={!!selectedUserId}
                    closing={closing}
                    onClose={() => handleUserClick(selectedUserId)}
                    detail={detail}
                    assignments={assignments}
                    loadingDetail={loadingDetail}
                    loadingAssign={loadingAssign}
                />
            )}

        </div>
    );

}
