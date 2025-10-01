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
        if (!file) return alert("파일을 선택해 주세요.");

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

    const sectionTitle = {
        margin: "30px 0 16px 0",
        fontSize: 18,
        fontWeight: 700,
        color: "#333",
    };

    const buttonBase = {
        padding: "10px 16px",
        borderRadius: 8,
        border: "none",
        cursor: "pointer",
        fontWeight: 600,
        fontSize: 14,
        transition: "all 0.2s",
        display: "inline-block",
        textAlign: "center",
        width: 140, // ✅ 고정 너비를 동일하게 설정
    };

    const primaryBtn = {
        ...buttonBase,
        background: "#289eff",
        color: "#fff",
    };

    const secondaryBtn = {
        ...buttonBase,
        background: "#f1f3f5",
        color: "#333",
    };

    return (
        <div style={{ maxWidth: 800, marginLeft: "20px" }}>
            {/* 제목 + 템플릿 다운로드 */}
            <div style={sectionTitle}>
                엑셀 업로드로 건물 등록
                <a
                    href="/template/excel_template.xlsx"
                    download
                    style={{
                        marginLeft: "10px",
                        display: "inline-block",
                        color: "#289eff",
                        fontSize: 14,
                        fontWeight: 600,
                        textDecoration: "none",
                    }}
                >
                    (템플릿 다운로드)
                </a>
            </div>

            {/* 파일 업로드 UI */}
            <div style={{ display: "flex", alignItems: "center", gap: "12px", marginBottom: 20 }}>
                {/* 숨겨진 input */}
                <input
                    id="fileUpload"
                    type="file"
                    accept=".xlsx"
                    style={{ display: "none" }}
                    onChange={handleFileChange}
                />

                {/* 커스텀 파일 선택 버튼 */}
                <label htmlFor="fileUpload" style={secondaryBtn}>
                    파일 선택
                </label>

                {/* 선택된 파일명 표시 */}
                <span style={{ fontSize: 14, color: "#555" }}>
                    {file ? file.name : "선택된 파일 없음"}
                </span>
            </div>

            {/* 업로드 버튼 */}
            <button style={primaryBtn} onClick={handleUpload}>
                업로드
            </button>

            {/* 결과 모달 */}
            <ResultModal
                show={showModal}
                onClose={() => setShowModal(false)}
                result={result || {}}
            />
        </div>
    );
}

export default BuildingUpload;
