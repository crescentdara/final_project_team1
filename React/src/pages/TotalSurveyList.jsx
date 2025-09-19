// src/pages/SurveyIndex.jsx
import {useEffect, useState} from "react";
import Pagination from "../components/ui/Pagination.jsx";

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
    if (v == null) continue;
    if (typeof v === "string") {
      if (v.trim() !== "") return v.trim();
    } else if (v !== "") {
      return v;
    }
  }
  return null;
};

// 서버 응답을 화면용으로 표준화
function adaptRow(x) {
  const id = firstNonBlank(x.id, x.buildingId, x.caseNo, x.manageNo);
  const caseNo = firstNonBlank(x.caseNo, x.manageNo, x.buildingId, id);
  const address = firstNonBlank(x.address, x.roadAddress, x.lotAddress) ?? "-";
  const investigatorId = firstNonBlank(x.assignedUserId, x.investigatorId);
  const investigatorName = firstNonBlank(x.assignedUserName, x.investigatorName) ?? "-";
  const derivedStatus =
      x.status ??
      (x.approved ? "APPROVED" :
          (typeof x.resultStatus === "string" ? x.resultStatus :
              (x.assigned ? "ASSIGNED" : "UNASSIGNED")));
  return { ...x, id, caseNo, address, investigatorId, investigatorName, status: derivedStatus };
}

export default function TotalSurveyList() {
  const [loading, setLoading] = useState(true);
  const [rows, setRows] = useState([]);
  const [total, setTotal] = useState(0);

  // 검색 상태
  const [status, setStatus] = useState("");
  const [investigatorId, setInvestigatorId] = useState("");   // 선택된 조사원 id
  const [keyword, setKeyword] = useState("");
  const [sort, setSort] = useState("latest");
  const [page, setPage] = useState(1);
  const size = 10;

  // 전체 조사원 목록을 백엔드에서 1회 로드 (페이지 rows에서 유추 X)
  const [investigators, setInvestigators] = useState([]);
  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        // 프로젝트에 맞는 "전체 조사원" 엔드포인트로 교체 가능
        const tryUrls = [
          "/web/building/investigators",              // 권장: 파라미터 없이 전원
          "/web/api/users?role=INVESTIGATOR"         // 대안
        ];
        for (const u of tryUrls) {
          const r = await fetch(u);
          if (!r.ok) continue;
          const data = await r.json();
          const arr = Array.isArray(data) ? data : (data.content ?? data.rows ?? []);
          if (!Array.isArray(arr)) continue;

          const normalized = arr.map(x => ({
            id: String(x.id ?? x.userId ?? x.value ?? ""),
            label: x.label ?? x.name ?? x.username ?? x.fullName ?? String(x.id ?? x.userId ?? "")
          })).filter(o => o.id); // id 없는 항목 제거
          normalized.sort((a, b) => a.label.localeCompare(b.label, "ko"));

          if (!cancelled) setInvestigators(normalized);
          return;
        }
        if (!cancelled) setInvestigators([]);
      } catch {
        if (!cancelled) setInvestigators([]);
      }
    })();
    return () => { cancelled = true; };
  }, []);

  const load = () => {
    setLoading(true);

    // 빈 값은 쿼리에서 제외 → 서버에서 param= 로 인한 400 방지
    const params = new URLSearchParams();
    if (status) params.append("status", status);                    // 상태 필터
    if (investigatorId) {
      // 서버 키가 무엇이든 대응하도록 둘 다 전송(필요 시 하나만 남겨도 됨)
      params.append("assignedUserId", investigatorId);              // 조사원 필터
      params.append("investigatorId", investigatorId);              // (호환)
    }
    if (keyword.trim()) params.append("keyword", keyword.trim());
    if (sort) params.append("sort", sort);
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

          setRows(content.map(adaptRow));
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

  // 상태/조사원/정렬/페이지 변경 시 자동 로드
  useEffect(() => { load(); /* eslint-disable-next-line */ }, [status, investigatorId, sort, page]);

  const onSearch = () => { setPage(1); load(); };

  const del = async (id) => {
    if (!confirm("삭제하시겠습니까?")) return;
    await fetch(`/web/building/surveys/${id}`, { method: "DELETE" });
    load();
  };

  // // 전체 페이지 수
  // const totalPages = useMemo(
  //     () => Math.max(1, Math.ceil(total / size)),
  //     [total, size]
  // );
  //
  // // 범위 유틸/페이지 아이템(… 포함)
  // const range = (start, end) =>
  //     Array.from({ length: Math.max(0, end - start + 1) }, (_, i) => start + i);
  //
  // const pageItems = useMemo(() => {
  //   const siblings = 1;
  //   const boundary = 1;
  //
  //   const startPages = range(1, Math.min(boundary, totalPages));
  //   const endPages   = range(Math.max(totalPages - boundary + 1, boundary + 1), totalPages);
  //
  //   const leftSiblingStart = Math.max(page - siblings, boundary + 1);
  //   const rightSiblingEnd  = Math.min(page + siblings, totalPages - boundary);
  //
  //   const showLeftDots  = leftSiblingStart > boundary + 1;
  //   const showRightDots = rightSiblingEnd  < totalPages - boundary;
  //
  //   const middle = range(leftSiblingStart, rightSiblingEnd);
  //
  //   const items = [...startPages];
  //   if (showLeftDots)  items.push("left-ellipsis");
  //   items.push(...middle);
  //   if (showRightDots) items.push("right-ellipsis");
  //   items.push(...endPages);
  //   return items;
  // }, [page, totalPages]);

  const createSurveyOnClick = () => {}

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

          {/* 전체 조사원 목록 사용 + 변경 시 page=1 리셋 */}
          <select
              className="form-select" style={{maxWidth:220}}
              value={investigatorId}
              onChange={(e)=>{ setInvestigatorId(e.target.value); setPage(1); }}
          >
            <option value="">조사원(선택)</option>
            {investigators.map(u => <option key={u.id} value={u.id}>{u.label}</option>)}
          </select>

          {/* 정렬은 유지/비활성화 선택 */}
          {/* <select className="form-select" style={{maxWidth:140}} value={sort} onChange={e=>{ setSort(e.target.value); setPage(1); }}>
          <option value="latest">최신 등록순</option>
          <option value="oldest">오래된 순</option>
        </select> */}

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
                <tr><td colSpan={5} className="text-center text-muted py-5">로딩중…</td></tr>
            ) : rows.length===0 ? (
                <tr><td colSpan={5} className="text-center text-muted py-5">표시할 데이터가 없습니다.</td></tr>
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
                  <td className="d-flex justify-content-between ps-3 pe-3">
                    <button className="btn btn-sm btn-outline-secondary" onClick={()=>alert(`수정: ${r.id ?? r.caseNo}`)}>수정</button>
                    <button className="btn btn-sm btn-outline-danger" onClick={()=>del(r.id ?? r.caseNo)}>삭제</button>
                  </td>
                </tr>
            ))}
            </tbody>
          </table>
        </div>

        {/* 페이지네이션 */}
        <Pagination
            page={page}
            total={total}
            size={size}
            onChange={setPage}
            siblings={1}           // 현재 페이지 양옆 1개씩 표시
            boundaries={1}         // 처음/끝 경계 1개 유지(= 1, 마지막)
            className="justify-content-center"
            lastAsLabel={false}     // 마지막 페이지를 '마지막'으로 표기 (원하면 false로)
        />
      </div>
  );
}
