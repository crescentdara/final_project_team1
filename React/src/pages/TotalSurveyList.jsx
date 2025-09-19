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

// 공백("")을 건너뛰고 첫 번째 '비어있지 않은' 값을 골라주는 헬퍼
const firstNonBlank = (...vals) => {
  for (const v of vals) {
    if (v == null) continue;                 // null/undefined 건너뜀
    if (typeof v === "string") {
      if (v.trim() !== "") return v.trim();  // 공백 문자열 건너뜀
    } else if (v !== "") {                   // 숫자 등 비문자 타입
      return v;
    }
  }
  return null;
};

// 서버 응답을 화면용으로 표준화할 때 firstNonBlank 사용
function adaptRow(x) {
  const id =
      firstNonBlank(x.id, x.buildingId, x.caseNo, x.manageNo);

  const caseNo =
      firstNonBlank(x.caseNo, x.manageNo, x.buildingId, id);

  const address =
      firstNonBlank(x.address, x.roadAddress, x.lotAddress) ?? "-";

  const investigatorId =
      firstNonBlank(x.assignedUserId, x.investigatorId);

  const investigatorName =
      firstNonBlank(x.assignedUserName, x.investigatorName) ?? "-";

  const derivedStatus =
      x.status ??
      (x.approved ? "APPROVED" :
          (typeof x.resultStatus === "string" ? x.resultStatus :
              (x.assigned ? "ASSIGNED" : "UNASSIGNED")));

  return {
    ...x,
    id,
    caseNo,
    address,
    investigatorId,
    investigatorName,
    status: derivedStatus,
  };
}

export default function TotalSurveyList() {
  const [loading, setLoading] = useState(true);
  const [rows, setRows] = useState([]);
  const [total, setTotal] = useState(0);

  // 검색 상태
  const [status, setStatus] = useState("");
  const [investigatorId, setInvestigatorId] = useState(""); // 선택된 조사원 id
  const [keyword, setKeyword] = useState("");
  const [sort, setSort] = useState("latest");
  const [page, setPage] = useState(1);
  const size = 10;

  // ✅ CHANGED: 별도 /investigators 호출 제거.
  // 현재 페이지 rows에서 조사원 목록을 유추(전역 목록이 필요하면 전용 API를 나중에 추가)
  const investigators = useMemo(() => {
    const map = new Map();
    rows.forEach(r => {
      if (r.investigatorId != null) {
        const id = String(r.investigatorId);
        const name = r.investigatorName ?? id;
        if (!map.has(id)) map.set(id, name);
      }
    });
    return Array.from(map.entries()).map(([id, label]) => ({ id, label }));
  }, [rows]);

  const load = () => {
    setLoading(true);

    const params = new URLSearchParams();
    if (status) params.append("status", status);
    if (keyword.trim()) params.append("keyword", keyword.trim());
    if (sort) params.append("sort", sort);
    if (investigatorId) {
      // ✅ CHANGED: 서버가 어떤 키를 받는지 모르면 둘 다 보냄(호환)
      params.append("assignedUserId", investigatorId);
      params.append("investigatorId", investigatorId);
    }
    params.append("page", String(page));
    params.append("size", String(size));

    fetch(`/web/building/surveys?${params.toString()}`)
        .then(r => {
          if (!r.ok) throw new Error(`HTTP ${r.status}`);
          return r.json();
        })
        .then(d => {
          const content = d.content ?? d.rows ?? d.data ?? [];
          const totalElements = d.totalElements ?? d.total ?? 0;
          const number0 = (typeof d.number === "number") ? d.number
              : (typeof d.page === "number" ? d.page - 1 : page - 1);

          const adapted = content.map(adaptRow); // ✅ CHANGED: 표준화 적용
          setRows(adapted);
          setTotal(totalElements);
          if (typeof d.number === "number" || typeof d.page === "number") {
            setPage(number0 + 1);
          }
        })
        .catch(err => {
          console.error("surveys fetch failed:", err);
          setRows([]); setTotal(0);
        })
        .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [status, investigatorId, sort, page]);

  const onSearch = () => { setPage(1); load(); };

  const del = async (id) => {
    if (!confirm("삭제하시겠습니까?")) return;
    await fetch(`/web/building/surveys/${id}`, { method: "DELETE" });
    load();
  };

  const isLastPage = useMemo(() => (page * size) >= total, [page, size, total]);

  return (
      <div className="container py-4">
        <div className="d-flex flex-wrap gap-2 align-items-center mb-3">
          <h3 className="m-0 me-auto">조사목록 전체 내역</h3>

          <select className="form-select" style={{maxWidth:140}} value={status} onChange={e=>setStatus(e.target.value)}>
            {statusOptions.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>

          {/* ✅ CHANGED: investigators는 rows에서 유추한 옵션 사용, 값은 investigatorId */}
          <select className="form-select" style={{maxWidth:220}} value={investigatorId} onChange={e=>setInvestigatorId(e.target.value)}>
            <option value="">조사원(선택)</option>
            {investigators.map(u => <option key={u.id} value={u.id}>{u.label}</option>)}
          </select>

          <select className="form-select" style={{maxWidth:140}} value={sort} onChange={e=>setSort(e.target.value)}>
            <option value="latest">최신 등록순</option>
            <option value="oldest">오래된 순</option>
          </select>

          <div className="input-group" style={{maxWidth:360}}>
            <input
                className="form-control"
                placeholder="관리번호/주소/조사원 검색"
                value={keyword}
                onChange={e=>setKeyword(e.target.value)}
                onKeyDown={e=>e.key==="Enter" && onSearch()}
            />
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
            ) : rows.map(r => (
                <tr key={r.id ?? r.caseNo}>
                  <td className="fw-semibold">{r.caseNo}</td>
                  <td>{r.address}</td>
                  {/* ✅ CHANGED: assignedUserName을 investigatorName으로 표준화하여 표시 */}
                  <td>{r.investigatorName ?? "-"}</td>
                  <td>
                  <span className={`badge ${statusBadge(r.status)}`}>
                    {
                      r.status==='UNASSIGNED'?'미배정'
                        : r.status==='ASSIGNED'?'배정'
                        : r.status==='REWORK'?'재조사'
                        : '승인'
                    }
                  </span>
                  </td>
                  <td className="d-flex gap-2">
                    <button className="btn btn-sm btn-outline-secondary" onClick={()=>alert(`수정: ${r.id ?? r.caseNo}`)}>수정</button>
                    <button className="btn btn-sm btn-outline-danger" onClick={()=>del(r.id ?? r.caseNo)}>삭제</button>
                  </td>
                </tr>
            ))}
            </tbody>
          </table>
        </div>

        <nav className="mt-3">
          <ul className="pagination pagination-sm">
            <li className={`page-item ${page===1?'disabled':''}`}>
              <button className="page-link" onClick={()=>setPage(p=>Math.max(1,p-1))}>이전</button>
            </li>
            <li className="page-item"><span className="page-link bg-light">{page}</span></li>
            <li className={`page-item ${isLastPage ? 'disabled':''}`}>
              <button className="page-link" onClick={()=>setPage(p=>p+1)}>다음</button>
            </li>
          </ul>
        </nav>
      </div>
  );
}
