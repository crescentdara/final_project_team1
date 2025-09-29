import { useState } from "react";
import axios from "axios";
import ResultModal from "../components/ui/ResultModal.jsx";

function BuildingUpload() {
    const [file, setFile] = useState(null);
    const [result, setResult] = useState(null);
    const [showModal, setShowModal] = useState(false);

    const handleFileChange = (e) => {
        setFile(e.target.files[0]);
    };

    const handleUpload = async () => {
        if (!file) {
            alert("업로드할 파일을 선택하세요!");
            return;
        }

        const formData = new FormData();
        formData.append("file", file);

        try {
            const res = await axios.post("/web/building/upload-excel", formData, {
                headers: { "Content-Type": "multipart/form-data" },
            });

            setResult(res.data);
            setShowModal(true);
        } catch (err) {
            setResult({ error: "업로드 실패: " + err.message });
            setShowModal(true);
        }
    };

    return (
        <div
            className="d-flex justify-content-center align-items-center"
            style={{ minHeight: "100vh" }}
        >
            <div
                className="shadow"
                style={{
                    width: "640px",          // 📌 넓이 확장
                    borderRadius: "16px",
                    backgroundColor: "#fff",
                    padding: "40px",         // 📌 여백 크게
                }}
            >
                {/* 타이틀 */}
                <h2
                    className="mb-4 text-center"
                    style={{ color: "#6898FF", fontWeight: "bold" }}
                >
                    건물 다건 등록
                </h2>

                {/* 템플릿 다운로드 버튼 */}
                <a
                    href="/template/building_upload_template.xlsx"
                    download
                    className="btn btn-outline-secondary mb-4 w-100 fw-bold"
                    style={{
                        borderRadius: "8px",
                        padding: "12px",
                    }}
                >
                    템플릿 다운로드
                </a>

                {/* 파일 선택 */}
                <div className="mb-4">
                    <input
                        type="file"
                        accept=".xlsx"
                        className="form-control"
                        onChange={handleFileChange}
                        style={{ borderRadius: "8px", padding: "10px" }}
                    />
                    <small className="text-muted">
                        ※ .xlsx 형식의 엑셀 파일만 업로드 가능합니다.
                    </small>
                </div>

                {/* 업로드 버튼 */}
                <button
                    className="btn w-100 fw-bold"
                    style={{
                        backgroundColor: "#6898FF",
                        border: "none",
                        color: "#fff",
                        borderRadius: "8px",
                        padding: "14px",       // 📌 버튼 크게
                        fontSize: "1.1rem",    // 📌 글씨 키우기
                        boxShadow: "0 4px 12px rgba(104,152,255,0.3)",
                    }}
                    onClick={handleUpload}
                >
                    업로드
                </button>

                {/* 결과 모달 */}
                <ResultModal
                    show={showModal}
                    onClose={() => setShowModal(false)}
                    result={result || {}}
                />
            </div>
        </div>
    );
}

export default BuildingUpload;
