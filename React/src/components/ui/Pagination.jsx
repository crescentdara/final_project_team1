// src/components/ui/Pagination.jsx   ← 파일명/경로 통일 (오탈자 X)
import React, { useMemo } from "react";

export default function Pagination({
    page,
    total,                 // 아이템 총개수
    totalPages: tpProp,    // 총 페이지 수를 직접 줄 수도 있음
    size,                  // 페이지당 개수
    pageSize,              // ← ✅ page에서 잘못 넘겨도 작동하도록 alias 허용
    onChange,
    siblings = 1,
    boundaries = 1,
    className = "justify-content-center",
    labels = { prev: "이전", next: "다음", first: "처음", last: "마지막", ellipsis: "…" },
    showFirstLast = false,
    lastAsLabel = false,
  }) {
  const effSize = useMemo(
      () => Math.max(1, Number(size ?? pageSize) || 1),   // ← ✅ size/pageSize 중 있는 값 사용
      [size, pageSize]
  );

  // 총 페이지 계산: totalPages prop 우선, 없으면 total/effSize로 계산
  const totalPages = useMemo(() => {
    if (tpProp != null) return Math.max(1, Number(tpProp) || 1);
    const t = Math.max(0, Number(total) || 0);
    return Math.max(1, Math.ceil(t / effSize));
  }, [tpProp, total, effSize]);

  const clamp = (n) => Math.min(totalPages, Math.max(1, n));
  const go = (p) => { const n = clamp(p); if (n !== page) onChange?.(n); };

  const range = (start, end) =>
      Array.from({ length: Math.max(0, end - start + 1) }, (_, i) => start + i);

  const items = useMemo(() => {
    const startPages = range(1, Math.min(boundaries, totalPages));
    const endPages   = range(Math.max(totalPages - boundaries + 1, boundaries + 1), totalPages);
    const leftStart  = Math.max(page - siblings, boundaries + 1);
    const rightEnd   = Math.min(page + siblings, totalPages - boundaries);
    const showLeft   = leftStart > boundaries + 1;
    const showRight  = rightEnd  < totalPages - boundaries;
    const middle     = range(leftStart, rightEnd);

    const arr = [...startPages];
    if (showLeft)  arr.push("left-ellipsis");
    arr.push(...middle);
    if (showRight) arr.push("right-ellipsis");
    arr.push(...endPages);

    return lastAsLabel ? arr.filter((n) => n !== totalPages) : arr; // 중복 방지
  }, [page, totalPages, siblings, boundaries, lastAsLabel]);

  return (
      <nav className="mt-3" aria-label="pagination">
        <ul className={`pagination pagination-sm ${className}`}>
          {showFirstLast && (
              <li className={`page-item ${page === 1 ? "disabled" : ""}`}>
                <button className="page-link" onClick={() => go(1)}>{labels.first ?? "처음"}</button>
              </li>
          )}

          <li className={`page-item ${page === 1 ? "disabled" : ""}`}>
            <button className="page-link" onClick={() => go(page - 1)}>{labels.prev ?? "이전"}</button>
          </li>

          {items.map((it, idx) =>
              (it === "left-ellipsis" || it === "right-ellipsis") ? (
                  <li key={it + idx} className="page-item disabled"><span className="page-link">{labels.ellipsis ?? "…"}</span></li>
              ) : (
                  <li key={it} className={`page-item ${page === it ? "active" : ""}`}>
                    <button className="page-link" onClick={() => go(it)} aria-current={page === it ? "page" : undefined}>{it}</button>
                  </li>
              )
          )}

          {lastAsLabel && (
              <li className={`page-item ${page === totalPages ? "active" : ""}`}>
                <button className="page-link" onClick={() => go(totalPages)}>{String(totalPages)}</button>
              </li>
          )}

          <li className={`page-item ${page === totalPages ? "disabled" : ""}`}>
            <button className="page-link" onClick={() => go(page + 1)}>{labels.next ?? "다음"}</button>
          </li>

          {showFirstLast && !lastAsLabel && (
              <li className={`page-item ${page === totalPages ? "disabled" : ""}`}>
                <button className="page-link" onClick={() => go(totalPages)}>{labels.last ?? "마지막"}</button>
              </li>
          )}
        </ul>
      </nav>
  );
}
