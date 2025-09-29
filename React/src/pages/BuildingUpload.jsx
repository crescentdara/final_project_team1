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
        if (!file) return;

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
        <div className="container mt-4">
            <h2>엑셀 업로드로 건물 등록</h2>

            {/* 🔽 템플릿 다운로드 버튼 */}
            <a
                href="/template/building_upload_template.xlsx"
                download
                className="btn btn-outline-secondary mb-3"
            >
                템플릿 다운로드
            </a>

            <input
                type="file"
                accept=".xlsx"
                className="form-control mb-3"
                onChange={handleFileChange}
            />
            <button className="btn btn-primary" onClick={handleUpload}>
                업로드
            </button>

            <ResultModal
                show={showModal}
                onClose={() => setShowModal(false)}
                result={result || {}}
            />
        </div>
    );
}

export default BuildingUpload;
