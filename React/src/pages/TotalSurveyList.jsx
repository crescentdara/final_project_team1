// src/pages/SurveyIndex.jsx
import { useEffect, useMemo, useState } from "react";

const statusOptions = [
  { value: "", label: "전체 상태" },
  { value: "UNASSIGNED", label: "미배정" },
  { value: "ASSIGNED",   label: "배정" },
  { value: "REWORK",     label: "재조사" },
  { value: "APPROVED",   label: "승인" },
];

const statusBadge = (s) =>
    s==="UNASSIGNED" ? "bg-secondary" :
        s==="ASSIGNED"   ? "bg-info text-dark" :
            s==="REWORK"     ? "bg-warning text-dark" :
                s==="APPROVED"   ? "bg-success" : "bg-light text-dark";

export default function TotalSurveyList() {
  const [loading, setLoading] = useState(true);
  const [rows, setRows] = useState([]);
  const [total, setTotal] = useState(0);

  // 검색 상태
  const [status, setStatus] = useState("");
  const [investigatorId, setInvestigatorId] = useState("");
  const [keyword, setKeyword] = useState("");
  const [sort, setSort] = useState("latest");
  const [page, setPage] = useState(1);
  const size = 10;

  // 조사원 옵션 (간단 로딩)
  const [investigators, setInvestigators] = useState([]);
  useEffect(() => {
    fetch(`/web/api/surveys/investigators?q=`)
        .then(r=>r.json()).then(setInvestigators).catch(console.error);
  }, []);

  const load = () => {
    setLoading(true);
    const params = new URLSearchParams({
      status,
      investigatorId: investigatorId || "",
      keyword,
      sort,
      page: String(page),
      size: String(size)
    });
    fetch(`/web/api/surveys?${params.toString()}`)
        .then(r=>r.json())
        .then(({content,totalElements}) => { setRows(content||[]); setTotal(totalElements||0); })
        .catch(console.error)
        .finally(()=>setLoading(false));
  };

  // 초기 및 조건 변경 시 로드
  useEffect(() => { load(); /* eslint-disable-next-line */ }, [status, investigatorId, sort, page]);

  const onSearch = () => { setPage(1); load(); };

  const del = async (id) => {
    if (!confirm("삭제하시겠습니까?")) return;
    await fetch(`/web/api/surveys/${id}`, { method: "DELETE" });
    load();
  };

  return (
      <div className="container py-4">
        <div className="d-flex flex-wrap gap-2 align-items-center mb-3">
          <h3 className="m-0 me-auto">조사목록 전체 내역</h3>

          <select className="form-select" style={{maxWidth:140}} value={status} onChange={e=>setStatus(e.target.value)}>
            {statusOptions.map(o=><option key={o.value} value={o.value}>{o.label}</option>)}
          </select>

          <select className="form-select" style={{maxWidth:220}} value={investigatorId} onChange={e=>setInvestigatorId(e.target.value)}>
            <option value="">조사원(선택)</option>
            {investigators.map(u => <option key={u.id} value={u.id}>{u.label}</option>)}
          </select>

          <select className="form-select" style={{maxWidth:140}} value={sort} onChange={e=>setSort(e.target.value)}>
            <option value="latest">최신 등록순</option>
            <option value="oldest">오래된 순</option>
          </select>

          <div className="input-group" style={{maxWidth:360}}>
            <input className="form-control" placeholder="관리번호/주소/조사원 검색"
                   value={keyword} onChange={e=>setKeyword(e.target.value)}
                   onKeyDown={e=>e.key==="Enter" && onSearch()} />
            <button className="btn btn-outline-secondary" onClick={onSearch}>검색</button>
          </div>

          <button className="btn btn-primary" onClick={()=>alert("추가 모달/페이지 연결")}>추가</button>
        </div>

        <div className="table-responsive">
          <table className="table align-middle">
            <thead>
            <tr className="table-light">
              <th style={{width:120}}>관리번호</th>
              <th>주소</th>
              <th style={{width:160}}>조사원</th>
              <th style={{width:110}}>상태</th>
              <th style={{width:140}}>관리</th>
            </tr>
            </thead>
            <tbody>
            {loading ? (
                <tr><td colSpan={5} className="text-center text-muted py-5">로딩중…</td></tr>
            ) : rows.length===0 ? (
                <tr><td colSpan={5} className="text-center text-muted py-5">표시할 데이터가 없습니다.</td></tr>
            ) : rows.map(r=>(
                <tr key={r.id}>
                  <td className="fw-semibold">{r.caseNo}</td>
                  <td>{r.address}</td>
                  <td>{r.investigatorName ?? "-"}</td>
                  <td><span className={`badge ${statusBadge(r.status)}`}>
                {r.status==='UNASSIGNED'?'미배정':r.status==='ASSIGNED'?'배정':r.status==='REWORK'?'재조사':'승인'}
              </span></td>
                  <td className="d-flex gap-2">
                    <button className="btn btn-sm btn-outline-secondary" onClick={()=>alert(`수정: ${r.id}`)}>수정</button>
                    <button className="btn btn-sm btn-outline-danger" onClick={()=>del(r.id)}>삭제</button>
                  </td>
                </tr>
            ))}
            </tbody>
          </table>
        </div>

        {/* 페이지네이션 */}
        <nav className="mt-3">
          <ul className="pagination pagination-sm">
            <li className={`page-item ${page===1?'disabled':''}`}>
              <button className="page-link" onClick={()=>setPage(p=>Math.max(1,p-1))}>이전</button>
            </li>
            <li className="page-item"><span className="page-link bg-light">{page}</span></li>
            <li className={`page-item ${rows.length < size ? 'disabled':''}`}>
              <button className="page-link" onClick={()=>setPage(p=>p+1)}>다음</button>
            </li>
          </ul>
        </nav>
      </div>
  );
}
