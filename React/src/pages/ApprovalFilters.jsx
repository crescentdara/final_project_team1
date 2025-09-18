import { useEffect, useMemo, useState } from "react";
import "../components/modals/ResultModal.jsx";
import ResultModal from "../components/modals/ResultModal.jsx";

/** 상태 배지 */
function StatusBadge({ status }) {




  const map = {
    PENDING:  { cls: "bg-warning text-dark", label: "대기" },
    APPROVED: { cls: "bg-success",           label: "승인" },
    REJECTED: { cls: "bg-danger",            label: "반려" },
  };
  const s = map[status] || map.PENDING;
  return <span className={`badge ${s.cls}`}>{s.label}</span>;
}

function ApprovalFilters({ keyword, setKeyword, sort, setSort, onRefresh }) {
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
              onKeyDown={(e) => e.key === "Enter" && onRefresh()}
          />
          <button className="btn btn-outline-secondary" onClick={onRefresh}>검색</button>
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

  // 페이지네이션(필요 시)
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const pageSize = 10;

  const [modalOpen, setModalOpen] = useState(false);
  const [modalItem, setModalItem] = useState(null);

  // 더미 로딩 (API 연결 전)
  useEffect(() => {
    setLoading(true);
    const timer = setTimeout(() => {
      const mock = Array.from({ length: 6 }).map((_, i) => ({
        id: i + 1,
        caseNo: `M-${2300 + i}`,
        investigator: `조사원${i + 1}`,
        address: `서울시 강동구 ${i + 1}번지`,
        submittedAt: "2025-09-15",
        priority: i % 3 === 0 ? "높음" : "보통",
        status: "PENDING", // ✅ 초기 상태
      }));
      setItems(mock);
      setTotal(mock.length);
      setLoading(false);
    }, 400);
    return () => clearTimeout(timer);
  }, []);

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

  // 목록 새로고침 (실제 API 연동 자리)
  const refresh = () => {
    setLoading(true);
    // fetch(`/web/api/approvals?status=PENDING&keyword=${keyword}&sort=${sort}&page=${page}&size=${pageSize}`)
    //   .then(r => r.json())
    //   .then(({ content, totalElements }) => { setItems(content); setTotal(totalElements); })
    //   .finally(() => setLoading(false));
    setTimeout(() => setLoading(false), 300);
  };

  // ✅ 옵티미스틱 UI 업데이트 유틸
  const updateStatusLocal = (ids, nextStatus) => {
    setItems((prev) =>
        prev.map((it) => (ids.includes(it.id) ? { ...it, status: nextStatus } : it))
    );
  };

  const approveSelected = async () => {
    if (selected.size === 0) return;
    const ids = [...selected];

    // 1) 낙관적 업데이트 (즉시 초록 '승인' 표시)
    updateStatusLocal(ids, "APPROVED");
    setSelected(new Set());

    try {
      // 2) 백엔드 동기화 (있다면)
      // await fetch("/web/api/approvals/bulk/approve", {
      //   method: "PATCH",
      //   headers: { "Content-Type": "application/json" },
      //   body: JSON.stringify({ ids }),
      // }).then((r) => {
      //   if (!r.ok) throw new Error("approve failed");
      // });
    } catch (e) {
      console.error(e);
      // 3) 실패 시 롤백/리프레시 전략 중 택1
      // refresh();
      // 또는 롤백: updateStatusLocal(ids, "PENDING");
      alert("승인 처리 중 오류가 발생했습니다.");
    }
  };

  const approveOne = async (id) => {
    updateStatusLocal([id], "APPROVED");
    setModalOpen(false);
    try {
      // await fetch("/web/api/approvals/bulk/approve", { method: "PATCH", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ ids: [id] }) });
    } catch (e) {
      console.error(e);
      // 실패 시 필요한 경우 롤백 / refresh()
    }
  };

  const rejectSelected = async () => {
    if (selected.size === 0) return;
    const ids = [...selected];

    // 1) 낙관적 업데이트 (즉시 빨간 '반려' 표시)
    updateStatusLocal(ids, "REJECTED");
    setSelected(new Set());

    try {
      // 2) 백엔드 동기화 (있다면)
      // await fetch("/web/api/approvals/bulk/reject", {
      //   method: "PATCH",
      //   headers: { "Content-Type": "application/json" },
      //   body: JSON.stringify({ ids }),
      // }).then((r) => {
      //   if (!r.ok) throw new Error("reject failed");
      // });
    } catch (e) {
      console.error(e);
      // 3) 실패 시 롤백/리프레시
      // refresh();
      // 또는 롤백: updateStatusLocal(ids, "PENDING");
      alert("반려 처리 중 오류가 발생했습니다.");
    }
  };

  const rejectOne = async (id) => {
    updateStatusLocal([id], "REJECTED");
    setModalOpen(false);
    try {
      // await fetch("/web/api/approvals/bulk/reject", { method: "PATCH", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ ids: [id] }) });
    } catch (e) {
      console.error(e);
      // 실패 시 필요한 경우 롤백 / refresh()
    }
  };

  const openResult = async (id) => {
    // 서버 상세 조회 자리 (안드로이드 연동 전이라 임시로 목록의 아이템 사용)
    // const detail = await fetch(`/web/api/approvals/${id}`).then(r => r.json());
    const found = items.find(x => x.id === id);
    setModalItem(found ?? { id, caseNo: "-", investigator: "-", address: "-" });
    setModalOpen(true);
  };

  return (
      <div className="container py-4">
        {/* 상단 필터/검색 */}
        <ApprovalFilters
            keyword={keyword}
            setKeyword={setKeyword}
            sort={sort}
            setSort={setSort}
            onRefresh={refresh}
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

        {/* 페이지네이션(옵션) */}
        <nav className="mt-3">
          <ul className="pagination pagination-sm">
            <li className={`page-item ${page === 1 ? "disabled" : ""}`}>
              <button className="page-link" onClick={() => setPage((p) => Math.max(1, p - 1))}>이전</button>
            </li>
            <li className="page-item"><span className="page-link bg-light">{page}</span></li>
            <li className={`page-item ${items.length < pageSize ? "disabled" : ""}`}>
              <button className="page-link" onClick={() => setPage((p) => p + 1)}>다음</button>
            </li>
          </ul>
        </nav>

        {/* ✅ 조사 결과 모달 */}
        <ResultModal
            open={modalOpen}
            item={modalItem}
            onClose={() => setModalOpen(false)}
            onApprove={approveOne}
            onReject={rejectOne}
        />
      </div>
  );
}
