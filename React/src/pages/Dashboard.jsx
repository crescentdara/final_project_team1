import { useEffect, useState } from "react";
import { getDashboardStats } from "../api";
import { useNavigate } from "react-router-dom";

function Dashboard() {
    const navigate = useNavigate();
    const [stats, setStats] = useState(null);

    useEffect(() => {
        getDashboardStats()
            .then((data) => setStats(data))
            .catch((err) => console.error("통계 데이터 불러오기 실패:", err));
    }, []);

    if (!stats) return <p>로딩 중...</p>;

    // 📌 전체 건물 수 기준
    const totalBuildings = stats.totalBuildings;
    const getHeight = (value) => {
        if (totalBuildings === 0) return "0%";
        return `${(value / totalBuildings) * 100}%`;
    };

    return (
        <div
            style={{
                maxWidth: "900px",
                margin: "40px auto",
                padding: "30px",
                border: "1px solid #ddd",
                borderRadius: "16px",
                boxShadow: "0 4px 12px rgba(0,0,0,0.05)",
                background: "#fff",
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                gap: "40px",
            }}
        >
            {/* 왼쪽 영역 - 제목 / 진행률 / 버튼 */}
            <div style={{ flex: "1", textAlign: "left" }}>
                <div
                    style={{
                        display: "inline-block",
                        background: "#333",
                        color: "white",
                        padding: "8px 16px",
                        borderRadius: "6px",
                        marginBottom: "20px",
                        fontWeight: "bold",
                    }}
                >
                    전체 통계
                </div>

                <p style={{ margin: 0, color: "#666" }}>총 조사 진행률</p>
                <p style={{ fontSize: "42px", fontWeight: "bold", color: "#289eff" }}>
                    {stats.progressRate}%
                </p>

                <button
                    className="btn btn-primary"
                    style={{
                        padding: "10px 20px",
                        borderRadius: "8px",
                        fontWeight: "500",
                        marginTop: "10px",
                    }}
                    onClick={() => navigate("/approvals")}
                >
                    미결재 건 확인 →
                </button>
            </div>

            {/* 오른쪽 영역 - 캡슐 3개 */}
            <div
                style={{
                    flex: "1",
                    display: "flex",
                    justifyContent: "center",
                    gap: "40px",
                }}
            >
                {/* 조사 진행 (전체 건물 대비 배정률) */}
                <div style={{ textAlign: "center" }}>
                    <div
                        style={{
                            width: "70px",
                            height: "150px",
                            borderRadius: "35px",
                            background: "#f1f3f5",
                            overflow: "hidden",
                            display: "flex",
                            alignItems: "flex-end",
                            margin: "0 auto",
                        }}
                    >
                        <div
                            style={{
                                width: "100%",
                                height: getHeight(stats.assignedBuildings),
                                background: "#289eff",
                                transition: "height 0.6s ease",
                            }}
                        ></div>
                    </div>
                    <p style={{ marginTop: "10px", fontWeight: "500" }}>
                        배정률<br />
                        <span style={{ color: "#289eff", fontWeight: "bold" }}>
                            {stats.assignedBuildings}건
                        </span>
                    </p>
                </div>

                {/* 결재 대기 중 */}
                <div style={{ textAlign: "center" }}>
                    <div
                        style={{
                            width: "70px",
                            height: "150px",
                            borderRadius: "35px",
                            background: "#f1f3f5",
                            overflow: "hidden",
                            display: "flex",
                            alignItems: "flex-end",
                            margin: "0 auto",
                        }}
                    >
                        <div
                            style={{
                                width: "100%",
                                height: getHeight(stats.waitingApproval),
                                background: "#ffc107",
                                transition: "height 0.6s ease",
                            }}
                        ></div>
                    </div>
                    <p style={{ marginTop: "10px", fontWeight: "500" }}>
                        결재 대기<br />
                        <span style={{ color: "#ffc107", fontWeight: "bold" }}>
                            {stats.waitingApproval}건
                        </span>
                    </p>
                </div>

                {/* 결재 완료 */}
                <div style={{ textAlign: "center" }}>
                    <div
                        style={{
                            width: "70px",
                            height: "150px",
                            borderRadius: "35px",
                            background: "#f1f3f5",
                            overflow: "hidden",
                            display: "flex",
                            alignItems: "flex-end",
                            margin: "0 auto",
                        }}
                    >
                        <div
                            style={{
                                width: "100%",
                                height: getHeight(stats.approved),
                                background: "#28a745",
                                transition: "height 0.6s ease",
                            }}
                        ></div>
                    </div>
                    <p style={{ marginTop: "10px", fontWeight: "500" }}>
                        결재 완료<br />
                        <span style={{ color: "#28a745", fontWeight: "bold" }}>
                            {stats.approved}건
                        </span>
                    </p>
                </div>
            </div>
        </div>
    );
}

export default Dashboard;
