// // src/pages/SurveyIndex.jsx
// import {useEffect, useState} from "react";
// import Pagination from "../components/ui/Pagination.jsx";
// import BuildingDetailModal from "../components/modals/BuildingDetailModal.jsx";
//
// const statusOptions = [
//     { value: "ALL", label: "전체 상태" },
//     { value: "UNASSIGNED", label: "미배정" },
//     { value: "ASSIGNED",   label: "배정" },
//     { value: "PENDING",    label: "결재 대기" },
//     { value: "APPROVED",   label: "결재 완료" },
//     { value: "REJECTED",   label: "반려" },
// ];
//
// const statusBadge = (label) =>
//     label==="미배정"     ? "bg-secondary" :
//         label==="배정"       ? "bg-info text-dark" :
//             label==="결재 대기"  ? "bg-warning text-dark" :
//                 label==="결재 완료"  ? "bg-success" :
//                     label==="반려"       ? "bg-danger" : "bg-light text-dark";
//
// export default function SurveyIndex() {
//     const [loading, setLoading] = useState(true);
//     const [rows, setRows] = useState([]);
//     const [total, setTotal] = useState(0);
//
//     // 검색 상태
//     const [status, setStatus] = useState("ALL");
//     const [keyword, setKeyword] = useState("");
//     const [page, setPage] = useState(1);
//     const size = 10;
//
//     // 모달 상태
//     const [showModal, setShowModal] = useState(false);
//     const [selectedId, setSelectedId] = useState(null);
//
//     const load = () => {
//         setLoading(true);
//
//         const q = new URLSearchParams({ page, size });
//         if (status && status !== "ALL") q.append("filter", status);
//         if (keyword.trim()) q.append("keyword", keyword.trim());
//
//         fetch(`/web/building/surveys?${q.toString()}`)
//             .then(r => { if (!r.ok) throw new Error(`HTTP ${r.status}`); return r.json(); })
//             .then(d => {
//                 const content = d.content ?? d.rows ?? [];
//                 setRows(content);
//                 setTotal(d.totalElements ?? 0);
//             })
//             .catch(err => { console.error("fetch failed:", err); setRows([]); setTotal(0); })
//             .finally(() => setLoading(false));
//     };
//
//     useEffect(() => { load(); }, [status, page]);
//
//     const onSearch = () => { setPage(1); load(); };
//
//     return (
//         <div className="container py-4">
//             <div className="d-flex flex-wrap gap-2 align-items-center mb-3">
//                 <h3 className="m-0 me-auto">조사목록 전체 내역</h3>
//
//                 <select
//                     className="form-select" style={{maxWidth:140}}
//                     value={status}
//                     onChange={(e)=>{ setStatus(e.target.value); setPage(1); }}
//                 >
//                     {statusOptions.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
//                 </select>
//
//                 <div className="input-group" style={{maxWidth:360}}>
//                     <input
//                         className="form-control"
//                         placeholder="주소/조사원/결재자 검색"
//                         value={keyword}
//                         onChange={e=>setKeyword(e.target.value)}
//                         onKeyDown={e=>e.key==="Enter" && onSearch()}
//                     />
//                     <button className="btn btn-outline-secondary" onClick={onSearch}>검색</button>
//                 </div>
//             </div>
//
//             <div className="table-responsive">
//                 <table className="table align-middle">
//                     <thead>
//                     <tr className="table-light text-center">
//                         <th style={{width:100}}>ID</th>
//                         <th>주소</th>
//                         <th style={{width:140}}>조사원</th>
//                         <th style={{width:140}}>결재자</th>
//                         <th style={{width:120}}>상태</th>
//                     </tr>
//                     </thead>
//                     <tbody className="text-center">
//                     {loading ? (
//                         <tr><td colSpan={5} className="text-center text-muted py-5">로딩중…</td></tr>
//                     ) : rows.length===0 ? (
//                         <tr><td colSpan={5} className="text-center text-muted py-5">표시할 데이터가 없습니다.</td></tr>
//                     ) : rows.map(r => (
//                         <tr key={r.buildingId}
//                             style={{cursor:"pointer"}}
//                             onClick={()=>{ setSelectedId(r.buildingId); setShowModal(true); }}>
//                             <td className="fw-semibold">{r.buildingId}</td>
//                             <td>{r.lotAddress}</td>
//                             <td>{r.assignedUserName ?? "-"}</td>
//                             <td>{r.approverName ?? "-"}</td>
//                             <td>
//                 <span className={`badge ${statusBadge(r.statusLabel)}`}>
//                   {r.statusLabel}
//                 </span>
//                             </td>
//                         </tr>
//                     ))}
//                     </tbody>
//                 </table>
//             </div>
//
//             <Pagination
//                 page={page}
//                 total={total}
//                 size={size}
//                 onChange={setPage}
//                 siblings={1}
//                 boundaries={1}
//                 className="justify-content-center"
//                 lastAsLabel={false}
//             />
//
//             {/* 상세 모달 */}
//             {showModal && (
//                 <BuildingDetailModal
//                     id={selectedId}
//                     onClose={()=>setShowModal(false)}
//                 />
//             )}
//         </div>
//     );
// }


// src/pages/SurveyIndex.jsx
// src/pages/SurveyIndex.jsx
import {useEffect, useState} from "react";
import Pagination from "../components/ui/Pagination.jsx";
import BuildingDetailModal from "../components/modals/BuildingDetailModal.jsx"; // 모달 컴포넌트 import

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
                "bg-light text-dark";

export default function SurveyIndex() {
    const [loading, setLoading] = useState(true);
    const [rows, setRows] = useState([]);
    const [total, setTotal] = useState(0);
    const [selectedId, setSelectedId] = useState(null); // ✅ 선택된 건물 ID (모달 제어)

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
                    </tr>
                    </thead>
                    <tbody className="text-center">
                    {loading ? (
                        <tr><td colSpan={4} className="text-center text-muted py-5">로딩중…</td></tr>
                    ) : rows.length===0 ? (
                        <tr><td colSpan={4} className="text-center text-muted py-5">표시할 데이터가 없습니다.</td></tr>
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

