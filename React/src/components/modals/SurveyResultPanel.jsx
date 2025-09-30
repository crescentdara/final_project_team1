import React, { useState, useEffect } from "react";
import NaverMap from "../NaverMap.jsx";
import RejectReasonModal from "./RejectReasonModal.jsx";

const titleAddress = (it) =>
    it?.lotAddress || it?.roadAddress || it?.address || "-";

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

export default function SurveyResultPanel({
                                              id,
                                              item,
                                              loading,
                                              error,
                                              onClose,
                                              onApprove,
                                              onReject,
                                              open = true, // 부모에서 내려주는 토글 상태
                                          }) {
    const [showRejectModal, setShowRejectModal] = useState(false);

    useEffect(() => {
        if (id) {
            console.log("📌 패널 열린 대상 ID:", id);
        }
    }, [id]);

    return (
        <div
            className="detail-panel"
            style={{
                width: open ? "40%" : "0",
                minWidth: open ? "420px" : "0",
                opacity: open ? 1 : 0,
                background: "#fff",
                boxShadow: open ? "-2px 0 8px rgba(0,0,0,0.05)" : "none",
                borderLeft: open ? "1px solid #ddd" : "none",
                padding: open ? "20px" : "0",
                overflowY: "auto",
                transition: "all 0.3s ease",
                borderRadius: "12px 0 0 12px",
            }}
        >
            {/* 헤더 */}
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h5
                    className="m-0 text-truncate"
                    title={titleAddress(item)}
                    style={{ maxWidth: "calc(100% - 40px)" }}
                >
                    {titleAddress(item)}
                </h5>
                <button type="button" className="btn-close" onClick={onClose}></button>
            </div>

            {/* 로딩/에러 */}
            {loading && <div className="text-center py-5">불러오는 중...</div>}
            {!loading && error && (
                <div className="alert alert-danger">{error}</div>
            )}

            {/* 본문 */}
            {!loading && !error && item && (
                <>
                    {/* 상단 요약 */}
                    <div className="mb-3">
                        <div className="fw-semibold">{item.investigator ?? "-"}</div>
                        <div className="text-muted small">상태: {item.status ?? "-"}</div>
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

                    {/* 조사 항목 표 */}
                    <table className="table table-sm align-middle">
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
                    <div className="row g-3">
                        {[
                            { key: "extPhoto", title: "외부 사진" },
                            { key: "extEditPhoto", title: "외부 편집" },
                            { key: "intPhoto", title: "내부 사진" },
                            { key: "intEditPhoto", title: "내부 편집" },
                        ].map(({ key, title }) => (
                            <div className="col-md-6" key={key}>
                                <div className="border rounded p-2 h-100">
                                    <div className="small text-muted mb-2">{title}</div>
                                    {item[key] ? (
                                        <img
                                            src={item[key]}
                                            alt={title}
                                            className="img-fluid rounded"
                                            style={{
                                                objectFit: "cover",
                                                width: "100%",
                                                height: 200,
                                            }}
                                            onError={(e) => {
                                                e.currentTarget.style.opacity = 0.4;
                                                e.currentTarget.alt = "이미지 없음";
                                            }}
                                        />
                                    ) : (
                                        <div className="text-muted small">이미지 없음</div>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>

                    {/* 버튼 */}
                    <div className="d-flex gap-2 mt-4">
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
                </>
            )}

            {/* 반려 사유 입력 모달 */}
            <RejectReasonModal
                open={showRejectModal}
                onClose={() => setShowRejectModal(false)}
                onSubmit={(reason) => {
                    onReject(item?.id, reason);
                    setShowRejectModal(false);
                }}
            />
        </div>
    );
}
