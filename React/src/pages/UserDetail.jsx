import { useEffect, useState, useRef } from "react";
import axios from "axios";
import { fetchUsersPaged, fetchUserDetail, fetchUserAssignments } from "../api/users";
import UserDetailPanel from "../components/users/UserDetailPanel.jsx";

export default function UserDetail() {
    // ---------- state ----------
    const [users, setUsers] = useState([]);
    const [loadingUsers, setLoadingUsers] = useState(false);
    const [error, setError] = useState(null);

    const [search, setSearch] = useState("");
    const [field, setField] = useState("all");

    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const pageSize = 10;

    const [selectedUserId, setSelectedUserId] = useState(null);
    const [detail, setDetail] = useState(null);
    const [assignments, setAssignments] = useState([]);
    const [loadingDetail, setLoadingDetail] = useState(false);
    const [loadingAssign, setLoadingAssign] = useState(false);
    const [closing, setClosing] = useState(false);
    const [forceEdit, setForceEdit] = useState(false); // ✅ 수정모드로 열기 트리거

    // ---------- helpers ----------
    const roleLabel = (r) =>
        ({ ADMIN: "ADMIN", EDITOR: "EDITOR", RESEARCHER: "RESEARCHER" }[String(r || "").toUpperCase()] || r || "-");

    const statusBadge = (s) => {
        const v = String(s || "").toUpperCase();
        const map = {
            ACTIVE: { cls: "bg-primary", label: "활성" },
            INACTIVE: { cls: "bg-secondary", label: "비활성" },
            SUSPENDED: { cls: "bg-warning text-dark", label: "중지" },
        };
        return map[v] || { cls: "bg-light text-dark", label: v || "-" };
    };

    // ---------- list load ----------
    useEffect(() => {
        loadUsers(page, search, field);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [page]);

    async function loadUsers(pageIdx = 0, keyword = "", fld = "all") {
        setLoadingUsers(true);
        setError(null);
        try {
            const data = await fetchUsersPaged({ page: pageIdx, size: pageSize, field: fld, keyword });
            setUsers(data.content || []);
            setTotalPages(data.totalPages ?? 0);
            setTotalElements(data.totalElements ?? data.totalCount ?? (data.content?.length ?? 0));
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

    // ---------- detail load ----------
    useEffect(() => {
        if (!selectedUserId) {
            setDetail(null);
            setAssignments([]);
            return;
        }

        let alive = true;
        (async () => {
            try {
                setLoadingDetail(true);
                setLoadingAssign(true);
                const d = await fetchUserDetail(selectedUserId);
                if (alive) setDetail(d);
                const list = await fetchUserAssignments(selectedUserId);
                if (alive) setAssignments(list);
            } catch (e) {
                if (alive) setError(e);
            } finally {
                if (alive) {
                    setLoadingDetail(false);
                    setLoadingAssign(false);
                }
            }
        })();

        return () => {
            alive = false;
        };
    }, [selectedUserId]);

    const handleRowClick = (userId) => {
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

    // ✅ 관리 버튼: 수정(패널을 수정모드로 열기)
    const handleEditClick = (e, u) => {
        e.stopPropagation();
        if (selectedUserId !== u.userId) setSelectedUserId(u.userId);
        setForceEdit((v) => !v); // 수정모드 트리거
    };

    // ✅ 관리 버튼: 삭제(바로 삭제 후 목록 새로고침)
    const handleDeleteClick = async (e, u) => {
        e.stopPropagation();
        if (!window.confirm(`정말로 삭제하시겠습니까?\n(${u.name} / ${u.username})`)) return;
        try {
            await axios.delete(`/web/api/users/${u.userId}`);
            alert("삭제 완료");
            if (selectedUserId === u.userId) setSelectedUserId(null);
            loadUsers(page, search.trim(), field);
        } catch (err) {
            console.error(err);
            alert("삭제 실패: " + (err?.message || ""));
        }
    };

    // 패널 저장/삭제 콜백(패널 내부에서 수정/삭제했을 때 테이블 갱신)
    const refreshList = () => loadUsers(page, search.trim(), field);

    // ---------- pagination ----------
    const renderPagination = () => (
        <nav className="mt-3">
            <ul className="pagination justify-content-center">
                <li className={`page-item ${page === 0 ? "disabled" : ""}`}>
                    <button className="page-link" onClick={() => setPage((p) => p - 1)}>이전</button>
                </li>
                {Array.from({ length: totalPages }, (_, i) => (
                    <li key={i} className={`page-item ${page === i ? "active" : ""}`}>
                        <button className="page-link" onClick={() => setPage(i)}>{i + 1}</button>
                    </li>
                ))}
                <li className={`page-item ${page === totalPages - 1 ? "disabled" : ""}`}>
                    <button className="page-link" onClick={() => setPage((p) => p + 1)}>다음</button>
                </li>
            </ul>
        </nav>
    );

    // ---------- height fill (왼쪽 카드 = 초록 박스) ----------
    const leftCardRef = useRef(null);
    const [leftMinH, setLeftMinH] = useState("auto");
    useEffect(() => {
        const update = () => {
            if (!leftCardRef.current) return;
            const rect = leftCardRef.current.getBoundingClientRect();
            const bottomPadding = 16; // 카드 아래 여백 보정
            const h = Math.max(0, window.innerHeight - rect.top - bottomPadding);
            setLeftMinH(`${Math.ceil(h)}px`);
        };
        update();
        window.addEventListener("resize", update);
        window.addEventListener("scroll", update, { passive: true });
        return () => {
            window.removeEventListener("resize", update);
            window.removeEventListener("scroll", update);
        };
    }, []);

    // ---------- styles ----------
    const wrapperStyle = {
           display: "flex",
           gap: 20,
           alignItems: "stretch",
           flexWrap: "nowrap",
           marginTop: 16,
           height: "calc(100vh - 110px)",
           overflow: "hidden"
};
    const leftStyle = {
            flex: "1 1 auto",       // 남은 공간 전부
           minWidth: 500,          // ★ 최소 너비 500
           display: "flex",
           flexDirection: "column",
           height: "100%",         // wrapper와 동일 높이
           minHeight: 0            // 내부 스크롤용
    };
    const panelSticky = { position: "sticky", top: 0 };
    const theadSticky = { position: "sticky", top: 0, background: "#fff", zIndex: 1 };

    // ★ 표 컨테이너를 flex로 → 남은 공간을 채우고 내부만 스크롤
    const tableBox = {
        border: "1px solid #e7e7e7",
        borderRadius: 12,
        overflow: "hidden",
        background: "#fff",
        display: "flex",
        flexDirection: "column",
        flex: 1,
        minHeight: 0,
    };
    const tableScroll = {
        flex: 1,
        minHeight: 0,
        overflow: "auto",
    };

    return (
        <div className="container-fluid p-0" style={wrapperStyle}>
            {/* LEFT: 표 리스트 */}
            <div ref={leftCardRef} className="p-4 shadow-sm rounded-3 bg-white" style={leftStyle}>
                <h3 className="fw-bold mb-2" style={{ borderLeft: "4px solid #6898FF", paddingLeft: 12 }}>
                    조사원 상세정보 <small className="text-muted" style={{ fontSize: 14 }}>({totalElements}명)</small>
                </h3>

                {/* 검색영역 */}
                <div className="d-flex flex-wrap gap-2 align-items-center justify-content-end mb-3">
                    <select
                        className="form-select"
                        style={{ maxWidth: 120, height: 40 }}
                        value={field}
                        onChange={(e) => setField(e.target.value)}
                    >
                        <option value="all">전체</option>
                        <option value="name">이름</option>
                        <option value="username">아이디</option>
                        <option value="empNo">사번</option>
                    </select>

                    <div className="input-group input-group-sm" style={{ maxWidth: 320, height: 40 }}>
                        <input
                            type="text"
                            className="form-control"
                            placeholder="조사원 검색"
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                            onKeyDown={(e) => e.key === "Enter" && handleSearch()}
                        />
                        <button className="btn btn-outline-secondary" onClick={handleSearch}>검색</button>
                    </div>
                </div>

                {/* 표 */}
                {error && (
                    <div className="alert alert-danger mt-3">
                        데이터를 불러오지 못했습니다. {String(error.message || error)}
                    </div>
                )}

                {loadingUsers ? (
                    <div className="text-muted py-4">로딩 중…</div>
                ) : users.length === 0 ? (
                    <div className="text-center text-muted py-4">검색 결과가 없습니다.</div>
                ) : (
                    <div style={tableBox}>
                        <div style={tableScroll}>
                            <table className="table table-hover align-middle mb-0">
                                <thead style={theadSticky}>
                                <tr className="text-center">
                                    <th style={{ width: 60 }}>No</th>
                                    <th style={{ width: 160 }}>이름</th>
                                    <th style={{ width: 180 }}>아이디</th>
                                    <th style={{ width: 160 }}>사번</th>
                                    <th style={{ width: 160 }}>관리</th>
                                </tr>
                                </thead>
                                <tbody>
                                {users.map((u) => {
                                    const selected = selectedUserId === u.userId;
                                    return (
                                        <tr
                                            key={u.userId}
                                            role="button"
                                            onClick={() => handleRowClick(u.userId)}
                                            className="text-center"
                                            style={{
                                                userSelect: "none",
                                                background: selected ? "rgba(104,152,255,0.06)" : undefined,
                                                outline: selected ? "2px solid #6898FF" : "none",
                                                outlineOffset: -2,
                                                transition: "background 0.15s ease",
                                            }}
                                            title="상세 보기"
                                        >
                                            <td>{u.userId}</td>
                                            <td>{u.name}</td>
                                            <td className="text-muted">{u.username}</td>
                                            <td className="text-muted">{u.empNo || "-"}</td>
                                            <td>
                                                <div className="d-flex justify-content-center gap-2">
                                                    <button
                                                        className="btn btn-sm btn-outline-primary"
                                                        onClick={(e) => handleEditClick(e, u)}
                                                    >
                                                        수정
                                                    </button>
                                                    <button
                                                        className="btn btn-sm btn-outline-danger"
                                                        onClick={(e) => handleDeleteClick(e, u)}
                                                    >
                                                        삭제
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    );
                                })}
                                </tbody>
                            </table>
                        </div>
                        <div className="p-2">{renderPagination()}</div>
                    </div>
                )}
            </div>

            {/* RIGHT: 상세 패널 (스티키 고정) */}
            {selectedUserId && (
                <div style={{ flex: "0 0 400px", minWidth: 400, height: "100%" }}>
                    <div style={{panelSticky, top: 0}}>
                        <UserDetailPanel
                            isOpen={!!selectedUserId}
                            closing={closing}
                            onClose={() => handleRowClick(selectedUserId)}
                            detail={detail}
                            assignments={assignments}
                            loadingDetail={loadingDetail}
                            loadingAssign={loadingAssign}
                            // ✅ 추가: 수정모드로 열기 + 저장/삭제 후 목록 갱신
                            startEdit={forceEdit}
                            onSaved={refreshList}
                            onDeleted={() => {
                                setSelectedUserId(null);
                                refreshList();
                            }}
                        />
                    </div>
                </div>
            )}
        </div>
    );
}
