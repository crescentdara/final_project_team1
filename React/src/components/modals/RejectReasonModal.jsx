import React, { useState } from "react";

function RejectReasonModal({ open, onClose, onSubmit }) {
    const [reason, setReason] = useState("");

    if (!open) return null; // 안 열리면 렌더링 X

    const handleSave = () => {
        if (!reason.trim()) {
            alert("반려 사유를 입력해주세요.");
            return;
        }
        onSubmit(reason); // 부모에서 API 호출 처리
        setReason(""); // 입력값 초기화
        onClose(); // 닫기
    };

    return (
        <div
            className="modal d-block"
            tabIndex="-1"
            style={{ background: "rgba(0,0,0,.35)" }}
        >
            <div className="modal-dialog">
                <div className="modal-content">
                    {/* Header */}
                    <div className="modal-header">
                        <h5 className="modal-title">반려 사유 입력</h5>
                        <button type="button" className="btn-close" onClick={onClose} />
                    </div>

                    {/* Body */}
                    <div className="modal-body">
            <textarea
                className="form-control"
                rows="4"
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                placeholder="반려 사유를 입력하세요"
            />
                    </div>

                    {/* Footer */}
                    <div className="modal-footer">
                        <button className="btn btn-primary" onClick={handleSave}>
                            저장
                        </button>
                        <button className="btn btn-outline-secondary" onClick={onClose}>
                            취소
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default RejectReasonModal;
