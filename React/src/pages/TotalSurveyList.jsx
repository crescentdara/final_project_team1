// src/pages/SurveyIndex.jsx
import {useEffect, useState} from "react";
import Pagination from "../components/ui/Pagination.jsx";
import {useNavigate} from "react-router-dom";

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

// 공백("")을 건너뛰고 첫 번째 비어있지 않은 값
const firstNonBlank = (...vals) => {
  for (const v of vals) {
    if (v == null) continue;
    if (typeof v === "string") { if (v.trim() !== "") return v.trim(); }
    else if (v !== "") return v;
  }
  return null;
};

// ✅ NEW: 상태를 4종으로 표준화(서버 응답 형태 제각각 호환)
function normalizeStatus(x) {
  const raw = (x.status ?? x.resultStatus ?? "").toString().trim().toUpperCase();
  const alias = {
    PENDING: "ASSIGNED", SENT: "ASSIGNED", TEMP: "ASSIGNED",
    REWORK: "REWORK", APPROVED: "APPROVED", ASSIGNED: "ASSIGNED", UNASSIGNED: "UNASSIGNED",
  };
  if (alias[raw]) return alias[raw];
  if (x.approved === true) return "APPROVED";
  if (x.assigned === true) return "ASSIGNED";
  return "UNASSIGNED";
}

// ✅ REPLACED: 응답을 화면 공통 스키마로 표준화(중복 함수 제거, 하나만 사용)
function adaptRow(x) {
  const id = firstNonBlank(x.id, x.buildingId, x.caseNo, x.manageNo);
  const caseNo = firstNonBlank(x.caseNo, x.manageNo, x.buildingId, id);
  const address = firstNonBlank(x.address, x.roadAddress, x.lotAddress) ?? "-";
  const investigatorId = firstNonBlank(x.assignedUserId, x.investigatorId, x.userId);
  const investigatorName = firstNonBlank(
      x.assignedUsername, x.investigatorName, x.investigator, x.username
  ) ?? "-";

  return { ...x, id, caseNo, address, investigatorId, investigatorName, status: normalizeStatus(x) };
}

export default function TotalSurveyList() {
  const [loading, setLoading] = useState(true);
  const [rows, setRows] = useState([]);
  const [total, setTotal] = useState(0);
  const navigate = useNavigate();

  // 검색 상태
  const [status, setStatus] = useState("");
  const [investigatorId, setInvestigatorId] = useState(""); // 선택된 조사원 id
  const [keyword, setKeyword] = useState("");
  const [sort, setSort] = useState("latest");
  const [page, setPage] = useState(1);
  const size = 10;

  // ✅ NEW: 검색모드 플래그(상태/조사원/키워드 중 하나라도 있으면 검색 API 사용)
  const isSearchMode = Boolean(status || investigatorId || keyword.trim());

  // 전체 조사원 목록을 백엔드에서 1회 로드
  const [investigators, setInvestigators] = useState([]);
  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        // ✅ CHANGED: 서버 컨트롤러와 맞춘 조사원 목록 API
        const r = await fetch(`/web/api/approvals/investigators?q=`);
        if (!r.ok) throw new Error(`HTTP ${r.status}`);
        const data = await r.json();
        const arr = Array.isArray(data) ? data : (data.content ?? data.rows ?? []);
        const normalized = (arr || []).map(x => ({
          id: String(x.id ?? x.userId ?? x.value ?? ""),
          label: x.label ?? x.name ?? x.username ?? x.fullName ?? String(x.id ?? x.userId ?? "")
        })).filter(o => o.id);
        normalized.sort((a,b)=>a.label.localeCompare(b.label,"ko"));
        if (!cancelled) setInvestigators(normalized);
      } catch {
        if (!cancelled) setInvestigators([]);
      }
    })();
    return () => { cancelled = true; };
  }, []);

  const load = () => {
    setLoading(true);

    // ✅ 공통 파라미터
    const base = new URLSearchParams({ page: String(page), size: String(size) });
    if (sort) base.append("sort", sort);

    let url;
    if (isSearchMode) {
      // ✅ 검색모드 → /web/api/approvals
      const q = new URLSearchParams(base);
      if (status) q.append("status", status);
      if (investigatorId) q.append("investigatorId", investigatorId); // 하나로 고정
      if (keyword.trim()) q.append("keyword", keyword.trim());
      url = `/web/api/approvals?${q.toString()}`;
    } else {
      // ✅ 전체목록 → /web/building/surveys (필터 제외)
      url = `/web/building/surveys?${base.toString()}`;
    }

    fetch(url)
        .then(r => { if (!r.ok) throw new Error(`HTTP ${r.status}`); return r.json(); })
        .then(d => {
          const content = d.content ?? d.rows ?? d.data ?? [];
          const totalElements = d.totalElements ?? d.total ?? 0;
          const number0 = (typeof d.number === "number") ? d.number
              : (typeof d.page === "number" ? d.page - 1 : page - 1);
          setRows(content.map(adaptRow));
          setTotal(totalElements);
          if (typeof d.number === "number" || typeof d.page === "number") {
            setPage(number0 + 1);
          }
        })
        .catch(err => { console.error("fetch failed:", err); setRows([]); setTotal(0); })
        .finally(() => setLoading(false));
  };

  // 상태/조사원/정렬/페이지 변경 시 자동 로드
  useEffect(() => { load(); /* eslint-disable-next-line */ }, [status, investigatorId, sort, page, isSearchMode]);

  const onSearch = () => { setPage(1); load(); };

  const del = async (id) => {
    if (!confirm("삭제하시겠습니까?")) return;
    await fetch(`/web/building/surveys/${id}`, { method: "DELETE" });
    load();
  };

  const createSurveyOnClick = () => {
    navigate('/createSurvey');
  };

  return (
      <div className="container py-4">
        <div className="d-flex flex-wrap gap-2 align-items-center mb-3">
          <h3 className="m-0 me-auto">조사목록 전체 내역</h3>

          {/* 상태 변경 시 page=1로 리셋 */}
          <select
              className="form-select" style={{maxWidth:140}}
              value={status}
              onChange={(e)=>{ setStatus(e.target.value); setPage(1); }}
          >
            {statusOptions.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>

          {/* 조사원 변경 시 page=1 리셋 */}
          <select
              className="form-select" style={{maxWidth:220}}
              value={investigatorId}
              onChange={(e)=>{ setInvestigatorId(e.target.value); setPage(1); }}
          >
            <option value="">조사원(선택)</option>
            {investigators.map(u => <option key={u.id} value={u.id}>{u.label}</option>)}
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

          <button className="btn btn-primary" onClick={createSurveyOnClick}>추가</button>
        </div>

        <div className="table-responsive">
          <table className="table align-middle">
            <thead>
            <tr className="table-light text-center">
              <th style={{width:120}}>관리번호</th>
              <th>주소</th>
              <th style={{width:160}}>조사원</th>
              <th style={{width:110}}>상태</th>
              <th style={{width:140}}>관리</th>
            </tr>
            </thead>
            <tbody className="text-center">
            {loading ? (
                <tr><td colSpan={5} className="text-center text-muted py-3">로딩중…</td></tr>
            ) : rows.length===0 ? (
                <tr><td colSpan={5} className="text-center text-muted py-3">표시할 데이터가 없습니다.</td></tr>
            ) : rows.map(r => (
                <tr key={r.id ?? r.caseNo}>
                  <td className="fw-semibold">{r.caseNo}</td>
                  <td>{r.address}</td>
                  <td>{r.investigatorName ?? "-"}</td>
                  <td>
                <span className={`badge ${statusBadge(r.status)}`}>
                  {r.status==='UNASSIGNED'?'미배정'
                      : r.status==='ASSIGNED'?'배정'
                          : r.status==='REWORK'?'재조사'
                              : '승인'}
                </span>
                  </td>
                  <td className="d-flex justify-content-between ps-3 pe-3 py-3">
                    <button className="btn btn-sm btn-outline-secondary" onClick={()=>alert(`수정: ${r.id ?? r.caseNo}`)}>수정</button>
                    <button className="btn btn-sm btn-outline-danger" onClick={()=>del(r.id ?? r.caseNo)}>삭제</button>
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
      </div>
  );
}
