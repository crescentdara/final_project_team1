// src/pages/PendingApprovals.jsx
import { useEffect, useMemo, useState } from "react";
import ResultModal from "../components/modals/ResultModal.jsx";
import Pagination from "../components/ui/Pagination.jsx";

/** 상태 배지 */
function StatusBadge({ status }) {
  const map = {
    PENDING:  { cls: "bg-warning text-dark", label: "대기" },
    APPROVED: { cls: "bg-success",           label: "승인" },
    REJECTED: { cls: "bg-danger",            label: "반려" },
  };
  // ★ 변경: 서버에서 SENT/TEMP 같이 올 때도 배지 보이도록 방어
  const s = map[status?.toUpperCase()] || { cls: "bg-secondary", label: status || "미정" };
  return <span className={`badge ${s.cls}`}>{s.label}</span>;
}

function ApprovalFilters({ keyword, setKeyword, sort, setSort, onSearch }) {

  return (
      <div className="d-flex flex-wrap gap-2 align-items-center mb-3">
        <h3 className="m-0 me-auto">결재 대기 중</h3>

        <select
            className="form-select"
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
          <button className="btn btn-outline-secondary" onClick={onSearch}>검색</button>
        </div>
      </div>
  );
}

function ApprovalItem({ item, checked, onToggle, onOpenResult }) {
  return (
      <div className="border rounded-4 p-3 d-flex align-items-center justify-content-between mb-3">
        <div className="d-flex align-items-start gap-3">
          <input
              className="form-check-input mt-1"
              type="checkbox"
              checked={checked}
              onChange={() => onToggle(item.id)}
          />
          <div>
            <div className="fw-semibold">
              {item.caseNo} · {item.investigator} · {item.address}
            </div>
            <div className="text-muted small mt-1 d-flex align-items-center gap-2">
              <span>접수일 {item.submittedAt}</span>
              <span>·</span>
              <span>우선순위 {item.priority}</span>
              <span>·</span>
              <span>상태 <StatusBadge status={item.status} /></span>
            </div>
          </div>
        </div>

        <div className="d-flex gap-2">
          <button className="btn btn-outline-secondary" onClick={() => onOpenResult(item.id)}>
            조사 결과
          </button>
        </div>
      </div>
  );
}

function SkeletonList({ rows = 5 }) {
  return (
      <>
        {Array.from({ length: rows }).map((_, i) => (
            <div key={i} className="border rounded-4 p-3 mb-3 placeholder-glow">
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

  // 리스트 & 선택
  const [items, setItems] = useState([]);
  const [selected, setSelected] = useState(new Set());

  // 페이지네이션
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const pageSize = 10;

  // 모달
  const [modalOpen, setModalOpen] = useState(false);
  const [modalItem, setModalItem] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState(null);

  /** 서버에서 목록 로드 */
  const fetchApprovals = ({ requireKeyword = false } = {}) => {
    setLoading(true);

    const params = new URLSearchParams({
      // status: "PENDING",               // ✖ (항상 PENDING으로 필터되어 비었음)
      status: "",                          // ★ 변경: 상태 필터 제거(전체 보기)
      keyword: keyword ?? "",
      sort,
      page: String(page),
      size: String(pageSize),
      requireKeyword: requireKeyword ? "true" : "false",
    });

    fetch(`/web/api/approvals?${params.toString()}`)
        // ★ 변경: HTTP 에러 대비
        .then(async (r) => {
          if (!r.ok) throw new Error(`${r.status} ${await r.text()}`);
          return r.json();
        })
        // ★ 변경: content/totalElements 키로 안전 매핑
        .then((data) => {
          const content = data.content ?? data.rows ?? data.sample ?? [];
          const total = data.totalElements ?? data.total ?? content.length ?? 0;
          setItems(content);
          setTotal(total);
          setSelected(new Set()); // 새 결과 로드 시 선택 초기화
        })
        .catch((e) => console.error(e))
        .finally(() => setLoading(false));
  };


  // 초기 로드 + 정렬/페이지 바뀔 때
  useEffect(() => {
    // 초기 목록을 보여주려면 requireKeyword=false
    fetchApprovals({ requireKeyword: false });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sort, page]);

  const allChecked = useMemo(
      () => items.length > 0 && selected.size === items.length,
      [items, selected]
  );

  const toggleOne = (id) => {
    setSelected((prev) => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  };

  const toggleAll = () => {
    setSelected((prev) =>
        prev.size === items.length ? new Set() : new Set(items.map((x) => x.id))
    );
  };

  /** 옵티미스틱 상태 변경 (승인/반려) */
  const updateStatusLocal = (ids, nextStatus) => {
    setItems((prev) =>
        prev.map((it) => (ids.includes(it.id) ? { ...it, status: nextStatus } : it))
    );
  };

  /** 선택 승인/반려 (서버 연동은 주석 해제) */
  const approveSelected = async () => {
    if (selected.size === 0) return;
    const ids = [...selected];
    updateStatusLocal(ids, "APPROVED");
    setSelected(new Set());
    try {
      // await fetch("/web/api/approvals/bulk/approve", {
      //   method: "PATCH",
      //   headers: { "Content-Type": "application/json" },
      //   body: JSON.stringify({ ids }),
      // });
    } catch (e) {
      console.error(e);
      // fetchApprovals(); // 서버 기준으로 재동기화
    }
  };

  const rejectSelected = async () => {
    if (selected.size === 0) return;
    const ids = [...selected];
    updateStatusLocal(ids, "REJECTED");
    setSelected(new Set());
    try {
      // await fetch("/web/api/approvals/bulk/reject", {
      //   method: "PATCH",
      //   headers: { "Content-Type": "application/json" },
      //   body: JSON.stringify({ ids }),
      // });
    } catch (e) {
      console.error(e);
      // fetchApprovals();
    }
  };

  /** 단건 승인/반려 (모달) */
  const approveOne = async (id) => {
    updateStatusLocal([id], "APPROVED");
    setModalOpen(false);
    try {
      // await fetch("/web/api/approvals/bulk/approve", { method: "PATCH", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ ids: [id] }) });
    } catch (e) {
      console.error(e);
      // fetchApprovals();
    }
  };

  const rejectOne = async (id) => {
    updateStatusLocal([id], "REJECTED");
    setModalOpen(false);
    try {
      // await fetch("/web/api/approvals/bulk/reject", { method: "PATCH", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ ids: [id] }) });
    } catch (e) {
      console.error(e);
      // fetchApprovals();
    }
  };

  /** 상세 모달 열기 (안드로이드 연동 전: 리스트 항목 활용) */
  const openResult = async (id) => {                                // ★ 변경
    setModalOpen(true);                 // 먼저 모달부터 열고
    setDetailLoading(true);
    setDetailError(null);
    setModalItem(null);

    try {
      const res = await fetch(`/web/api/approvals/${id}`, { headers: { Accept: "application/json" } });
      if (!res.ok) throw new Error(`${res.status} ${await res.text()}`);
      const detail = await res.json();  // SurveyResultDetailDto 모양 그대로
      setModalItem(detail);
    } catch (e) {
      console.error(e);
      setDetailError(e.message);
    } finally {
      setDetailLoading(false);
    }
  };

  /** 검색 버튼/Enter → 검색어 포함 결과만 */
  const onSearch = () => {
    setPage(1);
    fetchApprovals({ requireKeyword: true });
  };

  return (
      <div className="container py-4">
        <ApprovalFilters
            keyword={keyword}
            setKeyword={setKeyword}
            sort={sort}
            setSort={setSort}
            onSearch={onSearch}
        />

        {/* 일괄 작업 바 */}
        <div className="d-flex align-items-center gap-2 mb-2">
          <div className="form-check">
            <input className="form-check-input" type="checkbox" checked={allChecked} onChange={toggleAll} />
            <label className="form-check-label">전체 선택</label>
          </div>

          <div className="vr mx-2" />

          <button className="btn btn-outline-success btn-sm" onClick={approveSelected} disabled={selected.size === 0}>
            선택 승인
          </button>
          <button className="btn btn-outline-danger btn-sm" onClick={rejectSelected} disabled={selected.size === 0}>
            선택 반려
          </button>

          <div className="ms-auto small text-muted">
            총 {total}건 · 선택 {selected.size}건
          </div>
        </div>

        {/* 리스트 */}
        {loading ? (
            <SkeletonList rows={5} />
        ) : items.length === 0 ? (
            <div className="text-center text-muted py-5 border rounded-4">
              표시할 결재 문서가 없습니다.
            </div>
        ) : (
            items.map((it) => (
                <ApprovalItem
                    key={it.id}
                    item={it}
                    checked={selected.has(it.id)}
                    onToggle={toggleOne}
                    onOpenResult={openResult}
                />
            ))
        )}

        {/* 페이지네이션 */}
        <Pagination
          page={page}
          total={total}        // ✅ 아이템 총개수
          pageSize={pageSize}
          size={pageSize}      // ✅ 기본 prop 이름 사용(또는 pageSize 그대로도 작동함)
          onChange={setPage}
          siblings={1}
          boundaries={1}
          className="justify-content-center"
          lastAsLabel={false}
        />

        {/* 조사 결과 모달 */}
        <ResultModal
            open={modalOpen}
            item={modalItem}            // ★ 상세 DTO 주입
            loading={detailLoading}     // ★ 로딩 전달
            error={detailError}         // ★ 에러 전달
            onClose={() => setModalOpen(false)}
            onApprove={approveOne}
            onReject={rejectOne}
        />
      </div>
  );
}
