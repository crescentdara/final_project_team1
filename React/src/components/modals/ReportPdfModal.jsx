// src/components/modals/ReportPdfModal.jsx
import { useEffect, useState } from "react";

export default function ReportPdfModal({ reportId, onClose }) {
    const [pdfUrl, setPdfUrl] = useState(null);

    useEffect(() => {
        if (reportId) {
            setPdfUrl(`/web/api/report/${reportId}/pdf`);
        }
    }, [reportId]);

    if (!reportId) return null;

    return (
        <div
            className="modal fade show"
            style={{ display: "block", backgroundColor: "rgba(0,0,0,0.5)" }}
            tabIndex="-1"
            role="dialog"
        >
            <div
                className="modal-dialog modal-xl modal-dialog-centered"
                role="document"
                style={{ maxWidth: "90%", height: "90%" }}
            >
                <div className="modal-content" style={{ height: "100%" }}>
                    {/* 헤더 */}
                    <div className="modal-header d-flex justify-content-between align-items-center">
                        <h5 className="modal-title">결과 보고서</h5>
                        <div className="d-flex gap-2">
                            <a
                                className="btn btn-sm btn-outline-primary"
                                href={`/web/api/report/pdf/${reportId}?mode=attachment`}  // ✅ 똑같이 수정
                            >
                                다운로드
                            </a>

                            <button
                                type="button"
                                className="btn-close"
                                onClick={onClose}
                            ></button>
                        </div>
                    </div>

                    {/* 바디 */}
                    <div className="modal-body p-0" style={{ height: "100%" }}>
                        {pdfUrl ? (
                            <iframe
                                src={`/web/api/report/pdf/${reportId}`}
                                title="PDF Viewer"
                                style={{
                                    width: "100%",
                                    height: "100%",
                                    border: "none",
                                }}
                            />
                        ) : (
                            <div className="d-flex justify-content-center align-items-center h-100">
                                로딩 중...
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}
