import {useEffect, useState} from "react";
import Pagination from "../components/ui/Pagination.jsx";
import {useNavigate} from "react-router-dom";
import BuildingDetailPanel from "../components/modals/BuildingDetailPanel.jsx"; // ✅ 그대로 사용

const statusOptions = [
    {value: "ALL", label: "전체 상태"},
    {value: "UNASSIGNED", label: "미배정"},
    {value: "ASSIGNED", label: "배정"},
    {value: "APPROVED", label: "승인"},
];

const statusBadge = (label) =>
    label === "미배정" ? "bg-secondary" :
        label === "배정" ? "bg-info text-dark" :
            label === "결재 완료" ? "bg-success" :
                label === "반려" ? "bg-danger" :
                    "bg-light text-dark";

// ✅ 승인(결재 완료) 상태 판별
const isApprovedRow = (row) => {
    const s = String(row?.status ?? row?.statusLabel ?? "").trim();
    const su = s.toUpperCase();
    return su === "APPROVED" || s === "결재 완료" || s === "승인";
};

export default function SurveyIndex() {
    const [loading, setLoading] = useState(true);
    const [rows, setRows] = useState([]);
    const [total, setTotal] = useState(0);
    const [selectedId, setSelectedId] = useState(null);
    const [deletingId, setDeletingId] = useState(null);

    const navigate = useNavigate();

    // 검색 상태
    const [status, setStatus] = useState("ALL");
    const [keyword, setKeyword] = useState("");
    const [page, setPage] = useState(1);
    const size = 10;

    const load = () => {
        setLoading(true);
        const q = new URLSearchParams({page, size});
        if (status && status !== "ALL") q.append("filter", status);
        if (keyword.trim()) q.append("keyword", keyword.trim());

        fetch(`/web/building/surveys?${q.toString()}`)
            .then(r => {
                if (!r.ok) throw new Error(`HTTP ${r.status}`);
                return r.json();
            })
            .then(d => {
                const content = d.content ?? d.rows ?? [];
                setRows(content);
                setTotal(d.totalElements ?? 0);
            })
            .catch(err => {
                console.error("fetch failed:", err);
                setRows([]);
                setTotal(0);
            })
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        load();
    }, [status, page]);

    const onSearch = () => {
        setPage(1);
        load();
    };

    const handleEdit = (e, buildingId) => {
        e.stopPropagation();
        navigate(`/surveyRegister?tab=single&id=${buildingId}`);
    };

    const handleDelete = async (e, buildingId) => {
        e.stopPropagation();

        const row = rows.find(x => x.buildingId === buildingId);
        if (row && isApprovedRow(row)) {
            alert("승인 상태의 조사지(건물)는 삭제할 수 없습니다.");
            return;
        }

        if (!confirm("정말로 이 조사지(건물)를 완전히 삭제하시겠습니까?\n삭제 후 복구할 수 없습니다.")) return;

        try {
            setDeletingId(buildingId);
            const r = await fetch(`/web/building/${buildingId}`, {method: "DELETE"});
            if (!r.ok) {
                const text = await r.text().catch(() => "");
                alert(text || `삭제 실패 (HTTP ${r.status})`);
                return;
            }
            if (rows.length === 1 && page > 1) {
                setPage(p => p - 1);
            } else {
                load();
            }
        } finally {
            setDeletingId(null);
        }
    };

    return (
        // ✅ 전체 페이지 스크롤 차단: 뷰포트 기준 높이 고정 + overflow hidden
        <div
            className="container-fluid"
            style={{
                display: "flex",
                padding: 0,
                gap: "16px",
                alignItems: "stretch",
                height: "calc(100vh - 110px)", // 상단 헤더/알림 영역 높이에 맞춰 조정하세요
                overflow: "hidden",
                position: "relative",
                marginTop: "16px",
            }}
        >
            {/* ✅ 왼쪽: 목록 카드(파란 영역) - 내부만 스크롤 */}
            <div
                className="shadow-sm rounded-3 bg-white d-flex flex-column"
                style={{
                    flex: selectedId ? "0 0 60%" : "1 1 100%",
                    transition: "flex-basis 0.25s ease",
                    minWidth: 0,
                    height: "100%",
                    overflow: "hidden" // 카드 외부로 넘침 방지
                }}
            >
                {/* 헤더 영역: 고정 */}
                <div className="p-4">
                    <h3
                        className="fw-bold mb-3 d-flex align-items-center"
                        style={{borderLeft: "4px solid #6898FF", paddingLeft: "12px"}}
                    >
                        전체 조사지 리스트
                        <span className="ms-2 text-muted fs-6">(총 {total}개)</span>
                    </h3>

                    {/* 필터/검색: 고정 */}
                    <div className="d-flex flex-wrap gap-2 align-items-center justify-content-end">
                        <select
                            className="form-select"
                            style={{maxWidth: 130, height: 40}}
                            value={status}
                            onChange={(e) => {
                                setStatus(e.target.value);
                                setPage(1);
                            }}
                        >
                            {statusOptions.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
                        </select>

                        <div className="input-group" style={{maxWidth: 320, height: 40}}>
                            <input
                                className="form-control"
                                placeholder="주소 / 조사원 검색"
                                value={keyword}
                                onChange={e => setKeyword(e.target.value)}
                                onKeyDown={e => e.key === "Enter" && onSearch()}
                            />
                            <button className="btn btn-outline-secondary" onClick={onSearch}>검색</button>
                        </div>
                    </div>
                </div>

                <div className="flex-grow-1 " style={{minHeight: 0}}>
                    <div className="table-responsive px-4">
                        {/* table-layout: fixed 로 칼럼 너비 고정 → ellipsis 동작 안정화 */}
                        <table className="table align-middle mb-0" style={{tableLayout: "fixed"}}>
                            {/* 고정 폭 정의: ID / 조사원 / 상태는 좁게, 주소는 남은 공간 전부 */}
                            <colgroup>
                                <col style={{width: 80}}/>
                                {/* ID */}
                                <col/>
                                {/* 주소 (남은 공간) */}
                                <col style={{width: 80}}/>
                                {/* 조사원 */}
                                <col style={{width: 100}}/>
                                {/* 상태 */}
                            </colgroup>

                            <thead>
                            <tr className="table-light text-center">
                                <th className="text-nowrap">No</th>
                                <th className="text-nowrap">주소</th>
                                <th className="text-nowrap">조사원</th>
                                <th className="text-nowrap">상태</th>
                            </tr>
                            </thead>

                            <tbody className="text-center">
                            {loading ? (
                                <tr>
                                    <td colSpan={4} className="text-center text-muted py-5">로딩중…</td>
                                </tr>
                            ) : rows.length === 0 ? (
                                <tr>
                                    <td colSpan={4} className="text-center text-muted py-5">표시할 데이터가 없습니다.</td>
                                </tr>
                            ) : (
                                rows.map((r) => {
                                    const approved = isApprovedRow(r);
                                    return (
                                        <tr
                                            key={r.buildingId}
                                            className="py-0" // 테이블 tr은 padding이 없지만 의미상 표기
                                            style={{
                                                cursor: "pointer",
                                                backgroundColor: selectedId === r.buildingId ? "#f0f6ff" : "transparent",
                                            }}
                                            onClick={() =>
                                                setSelectedId((prev) => (prev === r.buildingId ? null : r.buildingId))
                                            }
                                        >
                                            {/* ID: 줄바꿈 방지로 높이 증가 방지 */}
                                            <td className="fw-semibold text-nowrap py-2">{r.buildingId}</td>

                                            {/* 주소: 한 줄 고정 + 넘치면 '…' (title로 전체값 툴팁 제공) */}
                                            <td className="py-2">
                                                <div className="text-truncate" title={r.lotAddress ?? "-"}>
                                                    {r.lotAddress ?? "-"}
                                                </div>
                                            </td>

                                            {/* 조사원/상태: 줄바꿈 방지 */}
                                            <td className="text-nowrap py-2">{r.assignedUserName ?? "-"}</td>
                                            <td className="text-nowrap py-2">
                              <span className={`badge ${statusBadge(r.statusLabel)}`}>
                              {(r.statusLabel === "결재 완료" ||
                                  r.statusLabel?.toUpperCase?.() === "APPROVED")
                                  ? "승인"
                                  : r.statusLabel}
                              </span>
                                            </td>
                                        </tr>
                                    );
                                })
                            )}
                            </tbody>
                        </table>
                    </div>
                </div>


                {/* ✅ 빨간 박스: 페이지네이션 - 카드 하단 고정 영역 */}
                <div className="p-2">
                    <Pagination
                        page={page}
                        total={total}
                        size={size}
                        onChange={setPage}
                        siblings={1}
                        boundaries={1}
                        className="justify-content-center"
                        lastAsLabel={false}
                    />
                </div>
            </div>

            {/* ✅ 오른쪽: 상세 패널(초록 영역) - 컴포넌트 그대로 유지 */}
            {selectedId && (
                <BuildingDetailPanel
                    id={selectedId}
                    onClose={() => setSelectedId(null)}
                    onEdit={(e) => handleEdit(e, selectedId)}
                    onDelete={(e) => handleDelete(e, selectedId)}
                    isApproved={!!rows.find(x => x.buildingId === selectedId && isApprovedRow(x))}
                    deleting={deletingId === selectedId}
                />
            )}
        </div>
    );
}
