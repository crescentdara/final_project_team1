// src/components/Pagination.jsx
import React, { useMemo } from "react";

/**
 * Props
 * - page            : 현재 페이지(1-base, 필수)
 * - total           : 아이템 총개수 (total 혹은 totalPages 둘 중 하나 제공)
 * - totalPages      : 총 페이지 수 (직접 줄 수도 있음)
 * - size            : 페이지당 아이템 수 (total 사용 시 필요)
 * - onChange(p)     : 페이지 변경 콜백(필수)
 * - siblings        : 현재 페이지 양옆 표시할 개수(기본 1) => 1 … 4 [5] 6 … 마지막
 * - boundaries      : 양끝 경계에 항상 표시할 페이지 수(기본 1) => '1', '마지막'
 * - className       : ul.pagination 에 추가할 클래스 (기본 'justify-content-center')
 * - labels          : { prev, next, first, last, ellipsis } (문구 커스터마이즈)
 * - showFirstLast   : '처음/마지막' 라벨 버튼 노출 여부(기본 false)
 * - lastAsLabel     : 마지막을 별도 버튼으로 렌더하되 '마지막' 대신 총 페이지 숫자를 표시(기본 false)
 *
 * 사용 예)
 * <Pagination
 *   page={page}
 *   total={totalElements}     // 또는 totalPages={totalPages}
 *   size={size}
 *   onChange={setPage}
 *   siblings={1}
 *   boundaries={1}
 *   className="justify-content-center"
 *   lastAsLabel={true}
 * />
 */
export default function Pagination({
    page,
    total,
    totalPages: tpProp,
    size,
    onChange,
    siblings = 1,
    boundaries = 1,
    className = "justify-content-center",
    labels = { prev: "이전", next: "다음", first: "처음", last: "마지막", ellipsis: "…" },
    showFirstLast = false,
    lastAsLabel = false,
    }) {
  // 총 페이지 계산: totalPages prop이 우선, 없으면 total/size로 계산
  const totalPages = useMemo(() => {
    if (tpProp != null) return Math.max(1, Number(tpProp) || 1);
    const t = Math.max(0, Number(total) || 0);
    const s = Math.max(1, Number(size) || 1);
    return Math.max(1, Math.ceil(t / s));
  }, [tpProp, total, size]);

  const clamp = (n) => Math.min(totalPages, Math.max(1, n));
  const go = (p) => {
    const nxt = clamp(p);
    if (nxt !== page) onChange?.(nxt);
  };

  const range = (start, end) =>
      Array.from({ length: Math.max(0, end - start + 1) }, (_, i) => start + i);

  // 표시할 번호/… 목록 만들기
  const items = useMemo(() => {
    const startPages = range(1, Math.min(boundaries, totalPages));
    const endPages = range(
        Math.max(totalPages - boundaries + 1, boundaries + 1),
        totalPages
    );

    const leftSiblingStart = Math.max(page - siblings, boundaries + 1);
    const rightSiblingEnd = Math.min(page + siblings, totalPages - boundaries);

    const showLeftDots = leftSiblingStart > boundaries + 1;
    const showRightDots = rightSiblingEnd < totalPages - boundaries;

    const middle = range(leftSiblingStart, rightSiblingEnd);

    const arr = [...startPages];
    if (showLeftDots) arr.push("left-ellipsis");
    arr.push(...middle);
    if (showRightDots) arr.push("right-ellipsis");
    arr.push(...endPages);

    // lastAsLabel=true이면 마지막 숫자 버튼은 숨기고(중복 방지)
    return lastAsLabel ? arr.filter((n) => n !== totalPages) : arr;
  }, [page, totalPages, siblings, boundaries, lastAsLabel]);

  return (
      <nav className="mt-3" aria-label="pagination">
        <ul className={`pagination pagination-sm ${className}`}>
          {/* 처음 (옵션) */}
          {showFirstLast && (
              <li className={`page-item ${page === 1 ? "disabled" : ""}`}>
                <button className="page-link" onClick={() => go(1)} aria-label="first">
                  {labels.first ?? "처음"}
                </button>
              </li>
          )}

          {/* 이전 */}
          <li className={`page-item ${page === 1 ? "disabled" : ""}`}>
            <button className="page-link" onClick={() => go(page - 1)} aria-label="previous">
              {labels.prev ?? "이전"}
            </button>
          </li>

          {/* 숫자 & … */}
          {items.map((it, idx) => {
            if (it === "left-ellipsis" || it === "right-ellipsis") {
              return (
                  <li key={it + idx} className="page-item disabled">
                    <span className="page-link">{labels.ellipsis ?? "…"}</span>
                  </li>
              );
            }
            const n = it; // number
            return (
                <li key={n} className={`page-item ${page === n ? "active" : ""}`}>
                  <button
                      className="page-link"
                      onClick={() => go(n)}
                      aria-current={page === n ? "page" : undefined}
                  >
                    {n}
                  </button>
                </li>
            );
          })}

          {/* 마지막을 숫자로 별도 버튼으로 표시 (옵션) */}
          {lastAsLabel && (
              <li className={`page-item ${page === totalPages ? "active" : ""}`}>
                <button className="page-link" onClick={() => go(totalPages)} aria-label="last-number">
                  {String(totalPages)}
                </button>
              </li>
          )}

          {/* 다음 */}
          <li className={`page-item ${page === totalPages ? "disabled" : ""}`}>
            <button className="page-link" onClick={() => go(page + 1)} aria-label="next">
              {labels.next ?? "다음"}
            </button>
          </li>

          {/* 마지막 (라벨 버튼, lastAsLabel=true일 땐 중복 방지 위해 숨김) */}
          {showFirstLast && !lastAsLabel && (
              <li className={`page-item ${page === totalPages ? "disabled" : ""}`}>
                <button className="page-link" onClick={() => go(totalPages)} aria-label="last">
                  {labels.last ?? "마지막"}
                </button>
              </li>
          )}
        </ul>
      </nav>
  );
}
