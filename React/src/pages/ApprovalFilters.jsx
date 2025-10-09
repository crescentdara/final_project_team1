// src/pages/PendingApprovals.jsx
import { useEffect, useRef, useState } from "react";
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

/** 상단 필터/검색 바 */
function ApprovalFilters({ keyword, setKeyword, sort, setSort, onSearch }) {
    return (
        <div className="w-100 align-items-center mb-3">
        <h3
            className="fw-bold "
            style={{ borderLeft: "4px solid #6898FF", paddingLeft: 12 }}
        >
            결재 대기
        </h3>

    {/* 오른쪽 정렬 */}
    <div className="ms-auto d-flex align-items-center gap-2 justify-content-end">
        <select
            className="form-select"
            style={{ maxWidth: 130, height: 40  }}
            value={sort}
            onChange={(e) => setSort(e.target.value)}
        >
            <option value="latest">최신 접수순</option>
            <option value="oldest">오래된 순</option>
        </select>

        <div className="input-group" style={{ maxWidth: 320, height: 40  }}>
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
</div>
);
}

function ApprovalItem({ item, onOpenResult }) {
    return (
        <div
            className="border rounded-4 p-3 d-flex align-items-center mb-3 bg-white shadow-sm"
            style={{ cursor: "pointer", transition: "all 0.2s" }}
            onClick={() => onOpenResult(item.id)}
        >
            <div className="d-flex w-100 align-items-center">
                <div className="fw-semibold me-3 text-truncate">
                    {item.caseNo} · {item.investigator} · {item.address}
                </div>
                <div className="ms-auto d-flex align-items-center gap-2 text-nowrap">
                    <small className="text-muted">접수일 {item.submittedAt}</small>
                    <StatusBadge status={item.status} />
                </div>
            </div>
        </div>
    );
}

function SkeletonList({ rows = 6 }) {
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
    const PAGE_SIZE = 6; // ✅ 6개만 보여주기

    // 패널
    const [selectedId, setSelectedId] = useState(null);
    const [detailItem, setDetailItem] = useState(null);
    const [detailLoading, setDetailLoading] = useState(false);
    const [detailError, setDetailError] = useState(null);

    // 카드 높이 = 뷰포트 하단까지 (스크롤 리스너 제거!)
    const cardRef = useRef(null);
    const [minH, setMinH] = useState("auto");
    useEffect(() => {
        const update = () => {
            if (!cardRef.current) return;
            const top = cardRef.current.getBoundingClientRect().top;
            const bottomPadding = 16;
            setMinH(`${Math.max(0, window.innerHeight - top - bottomPadding)}px`);
        };
        update(); // mount 시 1회
        window.addEventListener("resize", update); // 리사이즈에만 반응
        return () => window.removeEventListener("resize", update);
    }, []);

    /** 🔒 하드코딩된 결재자 ID */
    const HARDCODED_APPROVER_ID = 2;

    /** 서버에서 목록 로드 */
    const fetchApprovals = ({ requireKeyword = false } = {}) => {
        setLoading(true);

        const params = new URLSearchParams({
            status: "",
            keyword: keyword ?? "",
            sort,
            page: String(page),
            size: String(PAGE_SIZE), // ✅ 6개로 요청
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

    /** 단건 승인 */
    const approveOne = async (id) => {
        try {
            const res = await fetch(`/web/api/approval/${id}/approve`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ approverId: HARDCODED_APPROVER_ID }),
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

    /** 단건 반려 */
    const rejectOne = async (id, reason = "사유 없음") => {
        try {
            const res = await fetch(`/web/api/approval/${id}/reject`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ approverId: HARDCODED_APPROVER_ID, rejectReason: reason }),
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

    /** 상세 패널 열기 (토글) */
    const openResult = async (id) => {
        setSelectedId((prev) => (prev === id ? null : id));
        if (selectedId === id) return;

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

    /** 로컬 목록 상태 갱신 */
    const updateStatusLocal = (ids, next) => {
        setItems((prev) =>
            prev.map((it) => (ids.includes(it.id) ? { ...it, status: next } : it))
        );
    };

    // -------- 스타일: 카드/리스트 스크롤/푸터 고정 --------
    const leftCardStyle = {
        flex: selectedId ? "0 0 60%" : "1 1 100%",
        transition: "flex-basis 0.3s ease",
        display: "flex",
        flexDirection: "column",
        height: "100%"
        // minHeight: minH, // ✅ 화면 하단까지(스크롤 리스너 없음)
    };
    const listScroll = {
        flex: 1,
        minHeight: 0,
    };

    return (
        <div
            className="container-fluid pt-4 p-0 me-1"
            style={{ display: "flex", alignItems: "stretch", height: "100%" }}
        >
            {/* 왼쪽: 리스트 카드 */}
            <div ref={cardRef} className="p-4 pb-0 shadow-sm rounded-3 bg-white" style={leftCardStyle}>
                <ApprovalFilters
                    keyword={keyword}
                    setKeyword={setKeyword}
                    sort={sort}
                    setSort={setSort}
                    onSearch={onSearch}
                />

                {/* 스크롤 되는 목록 */}
                <div style={listScroll}>
                    {loading ? (
                        <SkeletonList rows={PAGE_SIZE} />
                    ) : items.length === 0 ? (
                        <div className="text-center text-muted py-5 border rounded-4 bg-light">
                            표시할 결재 문서가 없습니다.
                        </div>
                    ) : (
                        items.map((it) => (
                            <ApprovalItem key={it.id} item={it} onOpenResult={openResult} />
                        ))
                    )}
                </div>

                {/* 하단 고정 페이지네이션 */}
                <Pagination
                    page={page}
                    total={total}
                    pageSize={PAGE_SIZE}
                    size={PAGE_SIZE}
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
