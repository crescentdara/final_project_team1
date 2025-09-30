import React, { useState } from "react";
import NaverMap from "../NaverMap.jsx";
import RejectReasonModal from "./RejectReasonModal.jsx";

// 주소 타이틀
const titleAddress = (it) => it?.lotAddress || it?.roadAddress || it?.address || "-";

// 라벨/값 매핑
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

// ✅ 서버 베이스 URL
const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

// ✅ 상대 경로를 절대 URL로 보정
const toImageUrl = (v) => {
  if (!v) return null;
  if (/^https?:\/\//i.test(v)) return v; // 이미 절대경로
  if (v.startsWith("/")) return API_BASE + v; // "/upload/..." 형태
  return `${API_BASE}/upload/${v}`; // "파일명"만 온 경우
};

// ✅ 모달 아이템에서 결재자 ID 안전 추출
const extractApproverId = (it) =>
    it?.approver?.userId ??
    it?.approver?.id ??
    it?.approverId ??
    it?.approverUserId ??
    null;

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

  // 버튼 공통 핸들러에서 approverId 전달
  const handleApprove = () => {
    const approverId = extractApproverId(item);
    onApprove(item?.id, approverId); // 🔧 변경: approverId 함께 전달
  };
  const handleReject = (reason) => {
    const approverId = extractApproverId(item);
    onReject(item?.id, reason, approverId); // 🔧 변경: approverId 함께 전달
  };

  return (
      <>
        <div className="modal d-block" tabIndex="-1" style={{ background: "rgba(0,0,0,.35)" }}>
          <div className="modal-dialog modal-xl modal-dialog-scrollable">
            <div className="modal-content">
              <div className="modal-header">
                <h5
                    className="modal-title text-truncate"
                    title={titleAddress(item)}
                    style={{ maxWidth: "calc(100% - 40px)" }}
                >
                  {titleAddress(item)}
                </h5>
                <button type="button" className="btn-close" onClick={onClose} />
              </div>

              <div className="modal-body">
                {loading && <div className="text-center py-5">불러오는 중...</div>}
                {!loading && error && <div className="alert alert-danger">{error}</div>}

                {!loading && !error && item && (
                    <>
                      {/* 상단 요약 */}
                      <div className="mb-3">
                        <div className="fw-semibold">{item.investigator ?? "-"}</div>
                        <div className="text-muted small">상태: {item.status ?? "-"}</div>
                      </div>

                      {/* 코드 → 텍스트 표 */}
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

                      {/* 지도 */}
                      <div className="mt-4">
                        <h6 className="fw-semibold mb-2">위치</h6>
                        {item.latitude && item.longitude ? (
                            <NaverMap latitude={item.latitude} longitude={item.longitude} />
                        ) : (
                            <div className="text-muted small">위치 정보 없음</div>
                        )}
                      </div>
                    </>
                )}
              </div>

              <div className="modal-footer">
                <button className="btn btn-success" onClick={handleApprove} disabled={!item || loading}>
                  승인
                </button>
                <button
                    className="btn btn-danger"
                    onClick={() => setShowRejectModal(true)}
                    disabled={!item || loading}
                >
                  반려
                </button>
                <button className="btn btn-outline-secondary" onClick={onClose}>
                  닫기
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* 반려 사유 입력 모달 */}
        <RejectReasonModal
            open={showRejectModal}
            onClose={() => setShowRejectModal(false)}
            onSubmit={(reason) => {
              handleReject(reason); // ✅ approverId 포함 전송
              setShowRejectModal(false);
            }}
        />
      </>
  );
}
