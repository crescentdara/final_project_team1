import React, { useState, useEffect } from "react";
import NaverMap from "../NaverMap.jsx";
import RejectReasonModal from "./RejectReasonModal.jsx";

const titleAddress = (it) => it?.lotAddress || it?.roadAddress || it?.address || "-";

const label = {
  possible: "조사 가능 여부",
  adminUse: "행정목적 활용",
  idleRate: "유휴 비율",
  safety: "안전 등급",
  wall: "외벽 상태",
  roof: "옥상 상태",
  windowState: "창호 상태",
  parking: "주차 가능",
  entrance: "현관 상태",
  ceiling: "천장 상태",
  floor: "바닥 상태",
};

const codeText = {
  possible: (v) => (v === 1 ? "가능" : v === 2 ? "불가" : "-"),
  adminUse: (v) => ({ 1: "활용", 2: "일부활용", 3: "미활용" }[v] ?? "-"),
  idleRate: (v) => ({ 1: "0~10%", 2: "10~30%", 3: "30~50%", 4: "50%+" }[v] ?? "-"),
  safety: (v) => ({ 1: "A", 2: "B", 3: "C", 4: "D", 5: "E" }[v] ?? "-"),
  wall: (v) => ({ 1: "양호", 2: "보통", 3: "불량" }[v] ?? "-"),
  roof: (v) => ({ 1: "양호", 2: "보통", 3: "불량" }[v] ?? "-"),
  windowState: (v) => ({ 1: "양호", 2: "보통", 3: "불량" }[v] ?? "-"),
  parking: (v) => (v === 1 ? "가능" : v === 2 ? "불가" : "-"),
  entrance: (v) => ({ 1: "양호", 2: "보통", 3: "불량" }[v] ?? "-"),
  ceiling: (v) => ({ 1: "양호", 2: "보통", 3: "불량" }[v] ?? "-"),
  floor: (v) => ({ 1: "양호", 2: "보통", 3: "불량" }[v] ?? "-"),
};

const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const toImageUrl = (v) => {
  if (!v) return null;
  if (/^https?:\/\//i.test(v)) return v;
  if (v.startsWith("/")) return API_BASE + v;
  return `${API_BASE}/upload/${v}`;
};

export default function SurveyResultPanel({
                                            id,
                                            item,
                                            loading,
                                            error,
                                            onClose,
                                            onApprove,
                                            onReject,
                                            open = true,
                                          }) {
  const [showRejectModal, setShowRejectModal] = useState(false);

  // 패널 열릴 때 바디 스크롤 잠금
  useEffect(() => {
    if (open) {
      const prev = document.body.style.overflow;
      document.body.style.overflow = "hidden";
      return () => {
        document.body.style.overflow = prev || "";
      };
    }
  }, [open]);

  useEffect(() => {
    if (id) console.log("📌 패널 열린 대상 ID:", id);
  }, [id]);

  // 패널(고정 컨테이너)
  const panelStyle = {
    position: "sticky",
    top: 0,
    height: "85vh",
    width: open ? "40%" : "0",
    minWidth: open ? "420px" : "0",
    opacity: open ? 1 : 0,
    background: "#fff",
    boxShadow: open ? "-2px 0 8px rgba(0,0,0,0.05)" : "none",
    borderLeft: open ? "1px solid #ddd" : "none",
    marginLeft: open ? "20px" : 0,
    padding: open ? "20px" : "0",
    borderRadius: "12px 0 0 12px",
    transition: "all 0.3s ease",
    overflow: "hidden",      // 내부만 스크롤
    display: "flex",
    flexDirection: "column",
  };

  // 가운데 내용부(여기만 스크롤)
  const scrollAreaStyle = {
    flex: 1,
    minHeight: 0,
    overflowY: "auto",
    paddingBottom: 12,
  };

  // 이 파일 전용 CSS (스크롤바 숨김 + 하단 버튼 고정)
  const localCss = `
    /* 가운데 내용 스크롤 + 스크롤바 UI 숨김 */
    .srp-scroll {
      -ms-overflow-style: none;      /* IE/old Edge */
      scrollbar-width: none;         /* Firefox */
      -webkit-overflow-scrolling: touch;
      overscroll-behavior: contain;  /* 바디로 스크롤 전파 방지 */
    }
    .srp-scroll::-webkit-scrollbar { width: 0; height: 0; display: none; }

    /* 하단 버튼 바 — 항상 보이게 */
    .srp-footer {
      flex: 0 0 auto;
      position: sticky;
      bottom: 0;
      padding-top: 12px;
      border-top: 1px solid #eee;
      background: linear-gradient(to top, rgba(255,255,255,0.98), rgba(255,255,255,0.9));
      backdrop-filter: saturate(1.1) blur(1px);
      z-index: 1;
    }
  `;

  return (
      <>
        <style>{localCss}</style>

        <aside className="detail-panel" style={panelStyle} aria-hidden={!open}>
          {/* 헤더(고정) */}
          <div
              className="d-flex justify-content-between align-items-center mb-3"
              style={{ flex: "0 0 auto" }}
          >
            <h5
                className="m-0 text-truncate"
                title={titleAddress(item)}
                style={{ maxWidth: "calc(100% - 40px)" }}
            >
              {titleAddress(item)}
            </h5>
            <button type="button" className="btn-close" onClick={onClose}></button>
          </div>

          {/* 가운데 내용 — 이 영역만 스크롤 */}
          <div className="srp-scroll" style={scrollAreaStyle}>
            {loading && <div className="text-center py-5">불러오는 중...</div>}
            {!loading && error && <div className="alert alert-danger">{error}</div>}

            {!loading && !error && item && (
                <>
                  {/* 지도 */}
                  <div className="mt-4">
                    {item.latitude && item.longitude ? (
                        <div style={{ height: 300 }}>
                          <NaverMap latitude={item.latitude} longitude={item.longitude} />
                        </div>
                    ) : (
                        <div className="text-muted small">위치 정보 없음</div>
                    )}
                  </div>

                  {/* 조사 항목 표 */}
                  <table className="table table-sm align-middle mt-3">
                    <thead>
                    <tr className="table-light">
                      <th style={{ width: 220 }}>항목</th>
                      <th>값</th>
                    </tr>
                    </thead>
                    <tbody>
                    {Object.keys(label).map((k) => (
                        <tr key={k}>
                          <th className="text-muted">{label[k]}</th>
                          <td>{codeText[k](item[k])}</td>
                        </tr>
                    ))}
                    </tbody>
                  </table>

                  {/* 사진 미리보기 */}
                  <div className="row g-3 mt-1">
                    {[
                      { key: "extPhoto", title: "외부 사진" },
                      { key: "extEditPhoto", title: "외부 편집" },
                      { key: "intPhoto", title: "내부 사진" },
                      { key: "intEditPhoto", title: "내부 편집" },
                    ].map(({ key, title }) => {
                      const url = toImageUrl(item?.[key]);
                      return (
                          <div className="col-md-3" key={key}>
                            <div className="border rounded p-2 h-100">
                              <div className="small text-muted mb-2">{title}</div>
                              {url ? (
                                  <img
                                      src={url}
                                      alt={title}
                                      className="img-fluid rounded"
                                      style={{ objectFit: "cover", width: "100%", height: 180 }}
                                      onError={(e) => {
                                        e.currentTarget.style.opacity = 0.4;
                                        e.currentTarget.alt = "이미지 없음";
                                        e.currentTarget.removeAttribute("src");
                                      }}
                                  />
                              ) : (
                                  <div className="text-muted small">이미지 없음</div>
                              )}
                            </div>
                          </div>
                      );
                    })}
                  </div>
                </>
            )}
          </div>

          {/* 하단 고정 버튼 바 — 항상 보임 */}
          <div className="srp-footer">
            <div className="d-flex gap-2">
              <button
                  className="btn btn-success flex-fill"
                  onClick={() => onApprove(item?.id)}
                  disabled={!item || loading}
              >
                승인
              </button>
              <button
                  className="btn btn-danger flex-fill"
                  onClick={() => setShowRejectModal(true)}
                  disabled={!item || loading}
              >
                반려
              </button>
            </div>
          </div>

          {/* 반려 사유 입력 모달 */}
          <RejectReasonModal
              open={showRejectModal}
              onClose={() => setShowRejectModal(false)}
              onSubmit={(reason) => {
                onReject(item?.id, reason);
                setShowRejectModal(false);
              }}
          />
        </aside>
      </>
  );
}
