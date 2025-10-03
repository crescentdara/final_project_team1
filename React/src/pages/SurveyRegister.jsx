import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import CreateSurvey from "./CreateSurvey.jsx";
import BuildingUpload from "./BuildingUpload.jsx";

function SurveyRegister() {
    const [sp] = useSearchParams();
    const tabParam = sp.get("tab") || "single";
    const editingId = sp.get("id"); // ✅ 수정할 건물 ID

    const [activeTab, setActiveTab] = useState(tabParam);

    useEffect(() => {
        setActiveTab(tabParam);
    }, [tabParam]);

    const tabStyle = (isActive) => ({
        flex: 1,
        padding: "14px 0",
        cursor: "pointer",
        fontWeight: 600,
        borderBottom: isActive ? "3px solid #289eff" : "2px solid #eee",
        color: isActive ? "#289eff" : "#555",
        textAlign: "center",
        transition: "all 0.2s",
        background: isActive ? "#f9fbff" : "#fff",
    });

    return (
        <div>
            {/* 상단 탭 */}
            <div
                style={{
                  display: "flex",
                  justifyContent: "flex-start",
                  background: "#fff",
                  borderRadius: "16px 16px 0 0",
                  boxShadow: "0 2px 4px rgba(0,0,0,0.05)",
                  marginTop: "20px",
                }}
            >
                <div
                    style={{ ...tabStyle(activeTab === "single"), maxWidth: 220 }}
                    onClick={() => setActiveTab("single")}
                >
                    단건 등록
                </div>
                <div
                    style={{ ...tabStyle(activeTab === "bulk"), maxWidth: 220 }}
                    onClick={() => setActiveTab("bulk")}
                >
                    엑셀 업로드 등록
                </div>
            </div>

            {/* 컨텐츠 */}
            <div
                style={{
                    background: "#fff",
                    borderTop: "none",
                    borderRadius: "0 0 16px 16px",
                    padding: "24px 28px",
                }}
            >
                {activeTab === "single"
                    ? <CreateSurvey editingId={editingId} />
                    : <BuildingUpload />}
            </div>
        </div>
    );
}

export default SurveyRegister;
