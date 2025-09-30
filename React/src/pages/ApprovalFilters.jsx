// src/pages/PendingApprovals.jsx
import { useEffect, useState } from "react";
import Pagination from "../components/ui/Pagination.jsx";
import SurveyResultPanel from "../components/modals/SurveyResultPanel.jsx";

/** 상태 배지 */
function StatusBadge({ status }) {
    const map = {
        PENDING: { cls: "bg-warning text-dark", label: "대기" },
        APPROVED: { cls: "bg-success", label: "승인" },
        REJECTED: { cls: "bg-danger", label: "반려" },
    };
    const s =
        map[status?.toUpperCase()] || { cls: "bg-secondary", label: status || "미정" };
    return <span className={`badge ${s.cls}`}>{s.label}</span>;
}

function ApprovalFilters({ keyword, setKeyword, sort, setSort, onSearch }) {
    return (
        <div className="d-flex flex-wrap gap-2 mb-3 align-items-center">
            <h3
                className="fw-bold m-0 d-flex align-items-center"
                style={{ borderLeft: "4px solid #6898FF", paddingLeft: "12px" }}
            >
                결재 대기 중
            </h3>

            <select
                className="form-select ms-auto"
                style={{ maxWidth: 160 }}
                value={sort}
                onChange={(e) => setSort(e.target.value)}
            >
                <option value="latest">최신 접수순</option>
                <option value="oldest">오래된 순</option>
            </select>

            <div className="input-group" style={{ maxWidth: 360 }}>
                <input
                    className="form-control"
                    placeholder="관리번호 / 조사원 / 주소 검색"
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                    onKeyDown={(e) => e.key === "Enter" && onSearch()}
                />
                <button className="btn btn-outline-secondary" onClick={onSearch}>
                    검색
                </button>
            </div>
        </div>
    );
}

function ApprovalItem({ item, onOpenResult }) {
    return (
        <div
            className="border rounded-4 p-3 d-flex align-items-center justify-content-between mb-3 bg-white shadow-sm"
            style={{ cursor: "pointer", transition: "all 0.2s" }}
            onClick={() => onOpenResult(item.id)}
        >
            <div>
                <div className="fw-semibold">
                    {item.caseNo} · {item.investigator} · {item.address}
                </div>
                <div className="text-muted small mt-1 d-flex align-items-center gap-2">
                    <span>접수일 {item.submittedAt}</span>
                    <span>·</span>
                    <span>우선순위 {item.priority}</span>
                    <span>·</span>
                    <span>
                        상태 <StatusBadge status={item.status} />
                    </span>
                </div>
            </div>
        </div>
    );
}

function SkeletonList({ rows = 5 }) {
    return (
        <>
            {Array.from({ length: rows }).map((_, i) => (
                <div
                    key={i}
                    className="border rounded-4 p-3 mb-3 placeholder-glow bg-white shadow-sm"
                >
                    <span className="placeholder col-7 me-2" />
                    <span className="placeholder col-3" />
                    <div className="mt-2">
                        <span className="placeholder col-4 me-2" />
                        <span className="placeholder col-2" />
                    </div>
                </div>
            ))}
        </>
    );
}

export default function PendingApprovals() {
    // UI 상태
    const [keyword, setKeyword] = useState("");
    const [sort, setSort] = useState("latest");
    const [loading, setLoading] = useState(true);

    // 리스트
    const [items, setItems] = useState([]);

    // 페이지네이션
    const [page, setPage] = useState(1);
    const [total, setTotal] = useState(0);
    const pageSize = 10;

    // 패널
    const [selectedId, setSelectedId] = useState(null);
    const [detailItem, setDetailItem] = useState(null);
    const [detailLoading, setDetailLoading] = useState(false);
    const [detailError, setDetailError] = useState(null);

    /** 서버에서 목록 로드 */
    const fetchApprovals = ({ requireKeyword = false } = {}) => {
        setLoading(true);

        const params = new URLSearchParams({
            status: "",
            keyword: keyword ?? "",
            sort,
            page: String(page),
            size: String(pageSize),
            requireKeyword: requireKeyword ? "true" : "false",
        });

        fetch(`/web/api/approvals?${params.toString()}`)
            .then(async (r) => {
                if (!r.ok) throw new Error(`${r.status} ${await r.text()}`);
                return r.json();
            })
            .then((data) => {
                const content = data.content ?? data.rows ?? data.sample ?? [];
                const total = data.totalElements ?? data.total ?? content.length ?? 0;
                setItems(content);
                setTotal(total);
            })
            .catch((e) => console.error(e))
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        fetchApprovals({ requireKeyword: false });
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [sort, page]);

    /** 옵티미스틱 상태 변경 */
    const updateStatusLocal = (ids, nextStatus) => {
        setItems((prev) =>
            prev.map((it) => (ids.includes(it.id) ? { ...it, status: nextStatus } : it))
        );
    };

    /** ✅ 단건 승인 */
    const approveOne = async (id) => {
        try {
            const res = await fetch(`/web/api/approval/${id}/approve`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ approverId: 1 }), // TODO: 로그인 유저 ID 반영
            });
            if (!res.ok) throw new Error("승인 요청 실패");

            alert("결재가 승인되었습니다.");
            updateStatusLocal([id], "APPROVED");
            setSelectedId(null);
        } catch (e) {
            console.error(e);
            alert("승인 중 오류 발생");
        }
    };

    /** ✅ 단건 반려 */
    const rejectOne = async (id, reason = "사유 없음") => {
        try {
            const res = await fetch(`/web/api/approval/${id}/reject`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ approverId: 1, rejectReason: reason }),
            });
            if (!res.ok) throw new Error("반려 요청 실패");

            alert("결재가 반려되었습니다.");
            updateStatusLocal([id], "REJECTED");
            setSelectedId(null);
        } catch (e) {
            console.error(e);
            alert("반려 중 오류 발생");
        }
    };

    /** 상세 패널 열기 (토글 지원) */
    const openResult = async (id) => {
        setSelectedId((prev) => (prev === id ? null : id));

        if (selectedId === id) return; // 같은 항목 재클릭 → 닫기

        setDetailLoading(true);
        setDetailError(null);
        setDetailItem(null);

        try {
            const res = await fetch(`/web/api/approvals/${id}`, {
                headers: { Accept: "application/json" },
            });
            if (!res.ok) throw new Error(`${res.status} ${await res.text()}`);
            const detail = await res.json();
            setDetailItem(detail);
        } catch (e) {
            console.error(e);
            setDetailError(e.message);
        } finally {
            setDetailLoading(false);
        }
    };

    const onSearch = () => {
        setPage(1);
        fetchApprovals({ requireKeyword: true });
    };

    return (
        <div
            className="container-fluid py-4"
            style={{ display: "flex", gap: "20px", alignItems: "stretch" }}
        >
            {/* 왼쪽: 리스트 카드 */}
            <div
                className="p-4 shadow-sm rounded-3 bg-white"
                style={{
                    flex: selectedId ? "0 0 60%" : "1 1 100%",
                    transition: "flex-basis 0.3s ease",
                }}
            >
                <ApprovalFilters
                    keyword={keyword}
                    setKeyword={setKeyword}
                    sort={sort}
                    setSort={setSort}
                    onSearch={onSearch}
                />

                {loading ? (
                    <SkeletonList rows={5} />
                ) : items.length === 0 ? (
                    <div className="text-center text-muted py-5 border rounded-4 bg-light">
                        표시할 결재 문서가 없습니다.
                    </div>
                ) : (
                    items.map((it) => (
                        <ApprovalItem key={it.id} item={it} onOpenResult={openResult} />
                    ))
                )}

                <Pagination
                    page={page}
                    total={total}
                    pageSize={pageSize}
                    size={pageSize}
                    onChange={setPage}
                    siblings={1}
                    boundaries={1}
                    className="justify-content-center mt-3"
                    lastAsLabel={false}
                />
            </div>

            {/* 오른쪽: 상세 패널 */}
            <SurveyResultPanel
                id={selectedId}
                item={detailItem}
                loading={detailLoading}
                error={detailError}
                onClose={() => setSelectedId(null)}
                onApprove={approveOne}
                onReject={rejectOne}
                open={Boolean(selectedId)}
            />
        </div>
    );
}
