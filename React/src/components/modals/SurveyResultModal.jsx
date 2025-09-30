import React, { useState } from "react";
import NaverMap from "../NaverMap.jsx";
import RejectReasonModal from "./RejectReasonModal.jsx"; // ✅ 추가

const titleAddress = (it) =>
    (it?.lotAddress || it?.roadAddress || it?.address || "-");

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
  idleRate: (v) =>
      ({ 1: "0~10%", 2: "10~30%", 3: "30~50%", 4: "50%+" }[v] ?? "-"),
  safety: (v) => ({ 1: "A", 2: "B", 3: "C", 4: "D", 5: "E" }[v] ?? "-"),
  wall: (v) => ({ 1: "양호", 2: "보통", 3: "불량" }[v] ?? "-"),
  roof: (v) => ({ 1: "양호", 2: "보통", 3: "불량" }[v] ?? "-"),
  windowState: (v) => ({ 1: "양호", 2: "보통", 3: "불량" }[v] ?? "-"),
  parking: (v) => (v === 1 ? "가능" : v === 2 ? "불가" : "-"),
  entrance: (v) => ({ 1: "양호", 2: "보통", 3: "불량" }[v] ?? "-"),
  ceiling: (v) => ({ 1: "양호", 2: "보통", 3: "불량" }[v] ?? "-"),
  floor: (v) => ({ 1: "양호", 2: "보통", 3: "불량" }[v] ?? "-"),
};

// 🔧 변경점 #1: 서버 베이스를 붙여 절대 URL로 바꿔주는 함수
// .env에 VITE_API_BASE_URL=http://localhost:8080 처럼 설정해두면 환경에 따라 자동 적용됨
const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const toImageUrl = (v) => {
  if (!v) return null;
  if (/^https?:\/\//i.test(v)) return v;       // 이미 절대 URL이면 그대로
  if (v.startsWith("/")) return API_BASE + v;   // "/upload/..." → "http://..../upload/..."
  return `${API_BASE}/upload/${v}`;             // "파일명"만 온 경우까지 대비
};

export default function SurveyResultModal({
                                            open,
                                            item,
                                            loading,
                                            error,
                                            onClose,
                                            onApprove,
                                            onReject,
                                          }) {
  const [showRejectModal, setShowRejectModal] = useState(false);

  if (!open) return null;

  return (
      <>
        <div
            className="modal d-block"
            tabIndex="-1"
            style={{ background: "rgba(0,0,0,.35)" }}
        >
          <div className="modal-dialog modal-xl modal-dialog-scrollable">
            <div className="modal-content">
              <div className="modal-header">
                <h5
                    className="modal-title text-truncate"
                    title={titleAddress(item)}
                    style={{ maxWidth: "calc(100% - 40px)" }} // 닫기 버튼 폭만큼 줄여서 말줄임 안전
                >
                  {titleAddress(item)}
                </h5>
                <button type="button" className="btn-close" onClick={onClose} />
              </div>

              <div className="modal-body">
                {/* 로딩/에러 상태 */}
                {loading && (
                    <div className="text-center py-5">불러오는 중...</div>
                )}
                {!loading && error && (
                    <div className="alert alert-danger">{error}</div>
                )}

                {/* 본문 */}
                {!loading && !error && item && (
                    <>
                      {/* 상단 요약 */}
                      <div className="mb-3">
                        <div className="fw-semibold">
                          {item.investigator ?? "-"}
                        </div>
                        <div className="text-muted small">
                          상태: {item.status ?? "-"}
                        </div>
                      </div>

                      {/* 숫자형 코드 → 텍스트 매핑 표 */}
                      <table className="table table-sm align-middle">
                        <thead>
                        <tr className="table-light">
                          <th style={{ width: 220 }}>항목</th>
                          <th>값</th>
                        </tr>
                        </thead>
                        <tbody>
                        {[
                          "possible",
                          "adminUse",
                          "idleRate",
                          "safety",
                          "wall",
                          "roof",
                          "windowState",
                          "parking",
                          "entrance",
                          "ceiling",
                          "floor",
                        ].map((k) => (
                            <tr key={k}>
                              <th className="text-muted">{label[k]}</th>
                              <td>{codeText[k](item[k])}</td>
                            </tr>
                        ))}
                        </tbody>
                      </table>

                      {/* 사진 미리보기 */}
                      <div className="row g-3">
                        {[
                          { key: "extPhoto",     title: "외부 사진" },
                          { key: "extEditPhoto", title: "외부 편집" },
                          { key: "intPhoto",     title: "내부 사진" },
                          { key: "intEditPhoto", title: "내부 편집" },
                        ].map(({ key, title }) => {
                          const url = toImageUrl(item?.[key]); // 🔧 변경점 #2: 보정된 URL로 교체
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
                                            // 🔧 변경점 #3: 깨진 이미지 처리(시각적 힌트 + 소스 제거/대체)
                                            e.currentTarget.style.opacity = 0.4;
                                            e.currentTarget.alt = "이미지 없음";
                                            e.currentTarget.removeAttribute("src"); // or e.currentTarget.src = fallbackUrl;
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


                      {/* 🔹 지도 표시 */}
                      <div className="mt-4">
                        <h6 className="fw-semibold mb-2">위치</h6>
                        {item.latitude && item.longitude ? (
                            <NaverMap
                                latitude={item.latitude}
                                longitude={item.longitude}
                            />
                        ) : (
                            <div className="text-muted small">위치 정보 없음</div>
                        )}
                      </div>
                    </>
                )}
              </div>

              <div className="modal-footer">
                <button
                    className="btn btn-success"
                    onClick={() => onApprove(item?.id)}
                    disabled={!item || loading}
                >
                  승인
                </button>
                <button
                    className="btn btn-danger"
                    onClick={() => setShowRejectModal(true)} // ✅ 반려 모달 열기
                    disabled={!item || loading}
                >
                  반려
                </button>
                <button
                    className="btn btn-outline-secondary"
                    onClick={onClose}
                >
                  닫기
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* 🔹 반려 사유 입력 모달 */}
        <RejectReasonModal
            open={showRejectModal}
            onClose={() => setShowRejectModal(false)}
            onSubmit={(reason) => {
              onReject(item?.id, reason); // 부모에서 axios 호출 처리
              setShowRejectModal(false);
            }}
        />
      </>
  );
}
