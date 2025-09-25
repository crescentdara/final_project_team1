
import {useEffect, useState} from "react";
import Pagination from "../components/ui/Pagination.jsx";
import BuildingDetailModal from "../components/modals/BuildingDetailModal.jsx";
import {useNavigate} from "react-router-dom";

const statusOptions = [
    { value: "ALL", label: "전체 상태" },
    { value: "UNASSIGNED", label: "미배정" },
    { value: "ASSIGNED",   label: "배정" },
    { value: "APPROVED",   label: "승인" },
];

const statusBadge = (label) =>
    label==="미배정"     ? "bg-secondary" :
        label==="배정"       ? "bg-info text-dark" :
            label==="승인"       ? "bg-success" :
                label==="반려"    ? "bg-danger" :
                "bg-light text-dark";

export default function SurveyIndex() {
    const [loading, setLoading] = useState(true);
    const [rows, setRows] = useState([]);
    const [total, setTotal] = useState(0);
    const [selectedId, setSelectedId] = useState(null); // ✅ 선택된 건물 ID (모달 제어)
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
        e.stopPropagation(); // 행 클릭(모달 열림) 전파 방지
        // 생성페이지를 그대로 쓰되 id 쿼리스트링으로 편집모드 진입
        navigate(`/createSurvey?id=${buildingId}`);
    };

    const handleDelete = async (e, buildingId) => {
        e.stopPropagation(); // 행 클릭(모달)로 전파 방지
        if (!confirm("정말로 이 조사지(건물)를 완전히 삭제하시겠습니까?\n삭제 후 복구할 수 없습니다.")) return;

        try {
            setDeletingId(buildingId);
            const r = await fetch(`/web/building/${buildingId}`, { method: "DELETE" });
            if (!r.ok) {
                const text = await r.text().catch(()=> "");
                // FK 제약 충돌 등 서버 에러 메시지 토스트
                alert(text || `삭제 실패 (HTTP ${r.status})`);
                return;
            }
            // 현재 페이지에 항목이 1개뿐이면 이전 페이지로 이동(빈 페이지 방지)
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
        <div className="container py-4">
            <div className="d-flex flex-wrap gap-2 align-items-center mb-3">
                <h3 className="m-0 me-auto">조사목록 전체 내역</h3>

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
                    ) : rows.map(r => (
                        <tr key={r.buildingId}
                            style={{cursor:"pointer"}}
                            onClick={()=>setSelectedId(r.buildingId)} // ✅ 모달 열기
                        >
                            <td className="fw-semibold">{r.buildingId}</td>
                            <td>{r.lotAddress ?? "-"}</td>
                            <td>{r.assignedUserName ?? "-"}</td>
                            <td>
                                <span className={`badge ${statusBadge(r.statusLabel)}`}>
                                    {r.statusLabel}
                                </span>
                            </td>
                            <td>
                                <button
                                    className="btn btn-sm btn-outline-secondary me-3"
                                    onClick={(e)=>handleEdit(e, r.buildingId)}
                                >
                                    수정
                                </button>
                                <button
                                    className="btn btn-sm btn-outline-danger"
                                    disabled={deletingId === r.buildingId}
                                    onClick={(e)=>handleDelete(e, r.buildingId)}
                                >
                                    {deletingId === r.buildingId ? "삭제 중…" : "삭제"}
                                </button>
                            </td>
                        </tr>
                    ))}
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

            {/* ✅ 모달 렌더링 */}
            {selectedId && (
                <BuildingDetailModal
                    id={selectedId}
                    onClose={() => setSelectedId(null)}
                />
            )}
        </div>
    );
}

