import {useEffect, useState} from "react";
import Pagination from "../components/ui/Pagination.jsx";
import {useNavigate} from "react-router-dom";
import BuildingDetailPanel from "../components/modals/BuildingDetailPanel.jsx"; // ✅ 패널 그대로 유지

const statusOptions = [
    { value: "ALL",        label: "전체 상태" },
    { value: "UNASSIGNED", label: "미배정" },
    { value: "ASSIGNED",   label: "배정" },
    { value: "APPROVED",   label: "승인" },
];

const statusBadge = (label) =>
    label==="미배정"     ? "bg-secondary" :
        label==="배정"       ? "bg-info text-dark" :
            label==="결재 완료"  ? "bg-success" :
                label==="반려"       ? "bg-danger" :
                    "bg-light text-dark";

// ✅ 승인(결재 완료) 상태 판별 헬퍼
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

        const q = new URLSearchParams({ page, size });
        if (status && status !== "ALL") q.append("filter", status);
        if (keyword.trim()) q.append("keyword", keyword.trim());

        fetch(`/web/building/surveys?${q.toString()}`)
            .then(r => { if (!r.ok) throw new Error(`HTTP ${r.status}`); return r.json(); })
            .then(d => {
                const content = d.content ?? d.rows ?? [];
                setRows(content);
                setTotal(d.totalElements ?? 0);
            })
            .catch(err => { console.error("fetch failed:", err); setRows([]); setTotal(0); })
            .finally(() => setLoading(false));
    };

    useEffect(() => { load(); }, [status, page]);

    const onSearch = () => { setPage(1); load(); };

    const handleEdit = (e, buildingId) => {
        e.stopPropagation();
        // ✅ SurveyRegister 탭으로 이동하면서 id 전달
        navigate(`/surveyRegister?tab=single&id=${buildingId}`);
    };

    const handleDelete = async (e, buildingId) => {
        e.stopPropagation();

        // ✅ 승인건 방어
        const row = rows.find(x => x.buildingId === buildingId);
        if (row && isApprovedRow(row)) {
            alert("승인 상태의 조사지(건물)는 삭제할 수 없습니다.");
            return;
        }

        if (!confirm("정말로 이 조사지(건물)를 완전히 삭제하시겠습니까?\n삭제 후 복구할 수 없습니다.")) return;

        try {
            setDeletingId(buildingId);
            const r = await fetch(`/web/building/${buildingId}`, { method: "DELETE" });
            if (!r.ok) {
                const text = await r.text().catch(()=> "");
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
        <div className="container-fluid mt-4" style={{display:"flex", gap:"20px", alignItems: "stretch"}}>
            {/* 왼쪽: 목록 카드 */}
            <div
                className="p-4 shadow-sm rounded-3 bg-white"
                style={{
                    flex: selectedId ? "0 0 60%" : "1 1 100%",
                    transition:"flex-basis 0.3s ease",
                    height: "100%"
                }}
            >
                {/* 제목 */}
                <h3
                    className="fw-bold mb-4 d-flex align-items-center"
                    style={{ borderLeft: "4px solid #6898FF", paddingLeft: "12px" }}
                >
                    조사목록 전체 내역
                    <span className="ms-2 text-muted fs-6">(총 {total}개)</span>
                </h3>

                {/* 필터/검색 */}
                <div className="d-flex flex-wrap gap-2 align-items-center mb-3">
                    <select
                        className="form-select" style={{maxWidth:140}}
                        value={status}
                        onChange={(e)=>{ setStatus(e.target.value); setPage(1); }}
                    >
                        {statusOptions.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
                    </select>

                    <div className="input-group" style={{maxWidth:360}}>
                        <input
                            className="form-control"
                            placeholder="주소/조사원 검색"
                            value={keyword}
                            onChange={e=>setKeyword(e.target.value)}
                            onKeyDown={e=>e.key==="Enter" && onSearch()}
                        />
                        <button className="btn btn-outline-secondary" onClick={onSearch}>검색</button>
                    </div>
                </div>

                {/* 테이블 */}
                <div className="table-responsive">
                    <table className="table align-middle">
                        <thead>
                        <tr className="table-light text-center">
                            <th style={{width:100}}>ID</th>
                            <th>주소</th>
                            <th style={{width:140}}>조사원</th>
                            <th style={{width:120}}>상태</th>
                            <th style={{width:140}}>관리</th>
                        </tr>
                        </thead>
                        <tbody className="text-center">
                        {loading ? (
                            <tr><td colSpan={5} className="text-center text-muted py-5">로딩중…</td></tr>
                        ) : rows.length===0 ? (
                            <tr><td colSpan={5} className="text-center text-muted py-5">표시할 데이터가 없습니다.</td></tr>
                        ) : rows.map(r => {
                            const approved = isApprovedRow(r);
                            return (
                                <tr key={r.buildingId}
                                    style={{
                                        cursor:"pointer",
                                        backgroundColor: selectedId === r.buildingId ? "#f0f6ff" : "transparent"
                                    }}
                                    onClick={() =>
                                        setSelectedId(prev => prev === r.buildingId ? null : r.buildingId)
                                    }
                                >
                                    <td className="fw-semibold">{r.buildingId}</td>
                                    <td>{r.lotAddress ?? "-"}</td>
                                    <td>{r.assignedUserName ?? "-"}</td>
                                    <td>
                                        <span className={`badge ${statusBadge(r.statusLabel)}`}>
                                            {(r.statusLabel==="결재 완료" || r.statusLabel?.toUpperCase?.()==="APPROVED") ? "승인" : r.statusLabel}
                                        </span>
                                    </td>
                                    <td>
                                        <button
                                            className="btn btn-sm btn-outline-secondary me-3"
                                            onClick={(e)=>handleEdit(e, r.buildingId)}
                                            disabled={approved}
                                            title={approved ? "승인 건은 수정할 수 없습니다." : undefined}
                                        >
                                            수정
                                        </button>
                                        <button
                                            className="btn btn-sm btn-outline-danger"
                                            disabled={approved || deletingId === r.buildingId}
                                            onClick={(e)=>handleDelete(e, r.buildingId)}
                                            title={approved ? "승인 건은 삭제할 수 없습니다." : undefined}
                                        >
                                            {deletingId === r.buildingId ? "삭제 중…" : "삭제"}
                                        </button>
                                    </td>
                                </tr>
                            );
                        })}
                        </tbody>
                    </table>
                </div>

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

            {/* 오른쪽: 상세 패널 (그대로 유지) */}
            {selectedId && (
                <BuildingDetailPanel
                    id={selectedId}
                    onClose={() => setSelectedId(null)}
                />
            )}
        </div>
    );
}
