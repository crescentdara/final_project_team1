function ResultModal({ show, onClose, result }) {
    if (!show) return null;

    return (
        <div className="modal-backdrop">
            <div className="modal-content p-4">
                <h4>업로드 결과</h4>
                <p>성공: {result.successCount}건</p>
                <p>실패: {result.failCount}건</p>

                {result.failMessages?.length > 0 && (
                    <div>
                        <h6>실패 내역</h6>
                        <ul>
                            {result.failMessages.map((msg, idx) => (
                                <li key={idx}>{msg}</li>
                            ))}
                        </ul>
                    </div>
                )}

                <button className="btn btn-secondary mt-3" onClick={onClose}>
                    닫기
                </button>
            </div>

            {/* 🔽 이 컴포넌트 안에 CSS 넣음 */}
            <style>{`
                .modal-backdrop {
                  position: fixed;
                  top: 0;
                  left: 0;
                  width: 100%;
                  height: 100%;
                  background: rgba(0,0,0,0.5);
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  z-index: 999;
                }

                .modal-content {
                  background: #fff;
                  max-width: 400px;
                  width: 90%;
                  border-radius: 12px;
                  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
                  animation: fadeIn 0.3s ease-in-out;
                }

                @keyframes fadeIn {
                  from { opacity: 0; transform: translateY(-10px); }
                  to { opacity: 1; transform: translateY(0); }
                }
            `}</style>
        </div>
    );
}

export default ResultModal;
