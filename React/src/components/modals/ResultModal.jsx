// src/components/ResultModal.jsx
import React, { useEffect } from "react";

export default function ResultModal({ open, item, onClose, onApprove, onReject }) {
  // 배경 스크롤 잠금
  useEffect(() => {
    if (open) document.body.style.overflow = "hidden";
    return () => { document.body.style.overflow = ""; };
  }, [open]);

  if (!open) return null;

  return (
      <div
          className="position-fixed top-0 start-0 w-100 h-100"
          style={{ background: "rgba(0,0,0,0.45)", zIndex: 1050 }}
          onClick={onClose}
      >
        <div
            className="bg-white rounded-4 shadow position-absolute p-0"
            style={{ width: "min(920px, 92vw)", maxHeight: "88vh", left: "50%", top: "50%", transform: "translate(-50%, -50%)" }}
            onClick={(e) => e.stopPropagation()}
        >
          {/* Header */}
          <div className="d-flex align-items-center justify-content-between px-3 py-2 border-bottom rounded-top-4">
            <div className="fw-semibold">조사내역</div>
            <button className="btn btn-sm btn-light" onClick={onClose}>✕</button>
          </div>

          {/* Body */}
          <div className="p-3" style={{ overflow: "auto", maxHeight: "calc(88vh - 120px)" }}>
            {/* 임시 내용 (안드로이드 연동 전) */}
            <div className="mb-3">
              <div className="fw-semibold mb-1">기본 정보</div>
              <div className="small text-muted">
                관리번호 <span className="fw-semibold">{item?.caseNo}</span> · 조사원 <span className="fw-semibold">{item?.investigator}</span> · 주소 <span className="fw-semibold">{item?.address}</span>
              </div>
            </div>

            <div className="mb-3">
              <div className="fw-semibold mb-1">조사 내용</div>
              <div className="border rounded-3 p-3" style={{ minHeight: 180 }}>
                임시 텍스트: 현장 점검 결과 특이사항 없음. 구조물 외관 균열 미발견. 배수 상태 양호.
                사진·체크리스트는 안드로이드 연동 시 노출 예정.
              </div>
            </div>

            <div className="mb-2">
              <div className="fw-semibold mb-1">사진(샘플)</div>
              <div className="d-flex gap-2 flex-wrap">
                {Array.from({ length: 4 }).map((_, i) => (
                    <div key={i} className="bg-light border rounded-3" style={{ width: 140, height: 90 }} />
                ))}
              </div>
            </div>
          </div>

          {/* Footer */}
          <div className="d-flex justify-content-end gap-2 px-3 py-3 border-top rounded-bottom-4">
            <button className="btn btn-success" onClick={() => onApprove(item.id)}>승인</button>
            <button className="btn btn-danger"  onClick={() => onReject(item.id)}>반려</button>
          </div>
        </div>
      </div>
  );
}
