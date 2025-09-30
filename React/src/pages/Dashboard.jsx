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

    // ✅ 원본 값 그대로 사용
    const totalBuildings = stats.totalBuildings;

    // 색상
    const SKY = "#c2dbff";     // 캡슐 전체(배경)
    const ORANGE = "#f18257";  // 캡슐 진행/확정
    const YELLOW = "#ffdc38";
    const GREEN = "#3bc894";  // 캡슐 진행/확정// 캡슐 진행/확정
    const PROG_A = "#98c3f1";  // 진행률 바 (민트)
    const PROG_B = "#5993ec";  // 진행률 바 그라디언트

    // 높이/표시
    const getHeight = (v) => (!totalBuildings ? "0%" : `${(v / totalBuildings) * 100}%`);
    const getHeightBy = (v, base) => (!base ? "0%" : `${(v / base) * 100}%`);
    const compact = (v) => `${v}/${totalBuildings}`;
    const compactBy = (v, base) => `${v}/${base || 0}`;

    // 진행률 퍼센트 (0~100)
    const progressPct = Math.max(0, Math.min(100, Number(stats.progressRate)));

    return (
        <div
            style={{
                maxWidth: 1000,
                margin: "36px auto",
                padding: 28,
                borderRadius: 14,
                background: "#fff",
                boxShadow: "0 10px 30px rgba(16,24,40,0.06)",
                display: "grid",
                gridTemplateColumns: "1.1fr 1fr",
                gap: 25,
            }}
        >
            {/* 왼쪽: 총 조사 진행률 - 가로 막대 */}
            <div style={{ display: "flex", flexDirection: "column", justifyContent: "center" }}>
                <div
                    style={{
                        display: "inline-block",
                        width: "150px",
                        background: "black",
                        color: "#fff",
                        padding: "6px 12px",
                        borderRadius: 5,
                        textAlign: "center",
                        fontSize: 20,
                        fontWeight: 800,
                        letterSpacing: 0.3,
                        marginTop: 0,
                        marginBottom: "auto",
                        alignContent: "center",
                    }}
                >
                    전체 통계
                </div>

                <div style={{ marginBottom: 10, color: "#1d55ac", fontSize: 20, fontWeight: 600, marginLeft: 10  }}>총 조사 진행률</div>

                {/* 큰 진행률 바 */}
                <div
                    style={{
                        position: "relative",
                        width: "80%",
                        minWidth: "250px",
                        height: 70,              // 다른 그래프보다 큼
                        background: "#eef2ff",
                        borderRadius: 999,
                        overflow: "hidden",
                        boxShadow: "inset 0 1px 0 rgba(255,255,255,.7)",
                        marginBottom: 30,
                    }}
                    aria-label="총 조사 진행률"
                >
                    <div
                        style={{
                            position: "absolute",
                            top: 0,
                            left: 0,
                            height: "100%",
                            width: `${progressPct}%`,
                            background: `linear-gradient(90deg, ${PROG_A}, ${PROG_B})`,
                            borderRadius: 999,
                            transition: "width .6s ease",
                        }}
                    />
                    {/* 퍼센트 텍스트 */}
                    <div
                        style={{
                            position: "absolute",
                            right: 10,
                            top: "50%",
                            marginRight: 10,
                            transform: "translateY(-50%)",
                            fontWeight: 900,
                            fontSize: 16,
                            color: "#1d55ac",
                        }}
                    >
                        {stats.progressRate}%
                    </div>
                </div>

                {/* 범례: 색상 -> 의미 */}
                <div style={{ display: "flex", gap: 16, alignItems: "center", flexWrap: "wrap", marginLeft: 10, marginBottom:"auto"}}>
                    <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                        <span style={{ width: 14, height: 14, borderRadius: 4, background: SKY, border: "1px solid #e2e8f0" }} />
                        <span style={{ fontSize: 12, color: "#334155", fontWeight: 700 }}> : 전체</span>
                    </div>

                    <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                        <span style={{ width: 14, height: 14, borderRadius: 4, background: ORANGE, border: "1px solid #e2e8f0" }} />
                        <span style={{ fontSize: 12, color: "#334155", fontWeight: 700 }}> : 배정</span>
                    </div>

                    <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                        <span style={{ width: 14, height: 14, borderRadius: 4, background: YELLOW, border: "1px solid #e2e8f0" }} />
                        <span style={{ fontSize: 12, color: "#334155", fontWeight: 700 }}> : 대기</span>
                    </div>

                    <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                        <span style={{ width: 14, height: 14, borderRadius: 4, background: GREEN, border: "1px solid #e2e8f0" }} />
                        <span style={{ fontSize: 12, color: "#334155", fontWeight: 700 }}> : 완료</span>
                    </div>
                </div>
            </div>

            {/* 오른쪽: 캡슐 3개 (현행 색상 유지) */}
            <div
                style={{
                    display: "flex",
                    justifyContent: "space-around",
                    alignItems: "flex-end",
                    minHeight: 320,
                    gap: 15
                }}
            >
                {/* 배정률 (분모: 전체) */}
                <div style={{ textAlign: "center" }}>
                    <div
                        style={{
                            width: 100,
                            height: 300,
                            borderRadius: 24,
                            background: SKY,
                            overflow: "hidden",
                            display: "flex",
                            alignItems: "flex-end",
                            boxShadow: "inset 0 1px 0 rgba(255,255,255,.6)",
                        }}
                    >
                        <div
                            style={{
                                width: "100%",
                                height: getHeight(stats.assignedBuildings),
                                background: ORANGE,
                                transition: "height .6s ease",
                            }}
                        />
                    </div>
                    <div style={{ marginTop: 10, fontWeight: 800, fontSize: 14 }}>배정률</div>
                    <div style={{ fontWeight: 700, fontSize: 12, color: ORANGE }}>
                        {compact(stats.assignedBuildings)}
                    </div>
                </div>

                {/* 결재대기중 (분모: 배정건수) */}
                <div style={{ textAlign: "center" }}>
                    <div
                        style={{
                            width: 100,
                            height: 300,
                            borderRadius: 24,
                            background: SKY,
                            overflow: "hidden",
                            display: "flex",
                            alignItems: "flex-end",
                            boxShadow: "inset 0 1px 0 rgba(255,255,255,.6)",
                        }}
                    >
                        <div
                            style={{
                                width: "100%",
                                height: getHeightBy(stats.waitingApproval, stats.assignedBuildings),
                                background: YELLOW,
                                transition: "height .6s ease",
                            }}
                        />
                    </div>
                    <div style={{ marginTop: 10, fontWeight: 800, fontSize: 14 }}>결재대기중</div>
                    <div style={{ fontWeight: 700, fontSize: 12, color: YELLOW }}>
                        {compactBy(stats.waitingApproval, stats.assignedBuildings)}
                    </div>
                </div>

                {/* 결재완료 (분모: 배정건수) */}
                <div style={{ textAlign: "center", marginRight: "30px" }}>
                    <div
                        style={{
                            width: 100,
                            height: 300,
                            borderRadius: 24,
                            background: SKY,
                            overflow: "hidden",
                            display: "flex",
                            alignItems: "flex-end",
                            boxShadow: "inset 0 1px 0 rgba(255,255,255,.6)",
                        }}
                    >
                        <div
                            style={{
                                width: "100%",
                                height: getHeightBy(stats.approved, stats.assignedBuildings),
                                background: GREEN,
                                transition: "height .6s ease",
                            }}
                        />
                    </div>
                    <div style={{ marginTop: 10, fontWeight: 800, fontSize: 14 }}>결재완료</div>
                    <div style={{ fontWeight: 700, fontSize: 12, color: GREEN }}>
                        {compactBy(stats.approved, stats.assignedBuildings)}
                    </div>
                </div>
                <button
                    onClick={() => navigate("/approvals")}
                    style={{
                        alignSelf: "flex-start",
                        marginTop: "auto",
                        marginBottom: "10",
                        marginLeft: "40",
                        width: "130px",
                        height: "35px",
                        alignContent: "center",
                        textAlign: "center",
                        borderRadius: 10,
                        border: "1px solid #0f172a",
                        background: "#FFF",
                        color: "#000",
                        fontWeight: 800,
                        fontSize: 11,
                        cursor: "pointer",
                    }}
                >
                    미결재 건 확인 →
                </button>
            </div>

        </div>


    );
}

export default Dashboard;





// import { useEffect, useState } from "react";
// import { getDashboardStats } from "../api";
// import { useNavigate } from "react-router-dom";
//
// function Dashboard() {
//     const navigate = useNavigate();
//     const [stats, setStats] = useState(null);
//
//     useEffect(() => {
//         getDashboardStats()
//             .then((data) => setStats(data))
//             .catch((err) => console.error("통계 데이터 불러오기 실패:", err));
//     }, []);
//
//     if (!stats) return <p>로딩 중...</p>;
//
//     // 📌 전체 건물 수 기준
//     const totalBuildings = stats.totalBuildings;
//     const getHeight = (value) => {
//         if (totalBuildings === 0) return "0%";
//         return `${(value / totalBuildings) * 100}%`;
//     };
//
//     return (
//         <div
//             style={{
//                 maxWidth: "900px",
//                 margin: "40px auto",
//                 padding: "30px",
//                 border: "1px solid #ddd",
//                 borderRadius: "16px",
//                 boxShadow: "0 4px 12px rgba(0,0,0,0.05)",
//                 background: "#fff",
//                 display: "flex",
//                 justifyContent: "space-between",
//                 alignItems: "center",
//                 gap: "40px",
//             }}
//         >
//             {/* 왼쪽 영역 - 제목 / 진행률 / 버튼 */}
//             <div style={{ flex: "1", textAlign: "left" }}>
//                 <div
//                     style={{
//                         display: "inline-block",
//                         background: "#333",
//                         color: "white",
//                         padding: "8px 16px",
//                         borderRadius: "6px",
//                         marginBottom: "20px",
//                         fontWeight: "bold",
//                     }}
//                 >
//                     전체 통계
//                 </div>
//
//                 <p style={{ margin: 0, color: "#666" }}>총 조사 진행률</p>
//                 <p style={{ fontSize: "42px", fontWeight: "bold", color: "#289eff" }}>
//                     {stats.progressRate}%
//                 </p>
//
//                 <button
//                     className="btn btn-primary"
//                     style={{
//                         padding: "10px 20px",
//                         borderRadius: "8px",
//                         fontWeight: "500",
//                         marginTop: "10px",
//                     }}
//                     onClick={() => navigate("/approvals")}
//                 >
//                     미결재 건 확인 →
//                 </button>
//             </div>
//
//             {/* 오른쪽 영역 - 캡슐 3개 */}
//             <div
//                 style={{
//                     flex: "1",
//                     display: "flex",
//                     justifyContent: "center",
//                     gap: "40px",
//                 }}
//             >
//                 {/* 조사 진행 (전체 건물 대비 배정률) */}
//                 <div style={{ textAlign: "center" }}>
//                     <div
//                         style={{
//                             width: "70px",
//                             height: "150px",
//                             borderRadius: "35px",
//                             background: "#f1f3f5",
//                             overflow: "hidden",
//                             display: "flex",
//                             alignItems: "flex-end",
//                             margin: "0 auto",
//                         }}
//                     >
//                         <div
//                             style={{
//                                 width: "100%",
//                                 height: getHeight(stats.assignedBuildings),
//                                 background: "#289eff",
//                                 transition: "height 0.6s ease",
//                             }}
//                         ></div>
//                     </div>
//                     <p style={{ marginTop: "10px", fontWeight: "500" }}>
//                         배정률<br />
//                         <span style={{ color: "#289eff", fontWeight: "bold" }}>
//                             {stats.assignedBuildings}건
//                         </span>
//                     </p>
//                 </div>
//
//                 {/* 결재 대기 중 */}
//                 <div style={{ textAlign: "center" }}>
//                     <div
//                         style={{
//                             width: "70px",
//                             height: "150px",
//                             borderRadius: "35px",
//                             background: "#f1f3f5",
//                             overflow: "hidden",
//                             display: "flex",
//                             alignItems: "flex-end",
//                             margin: "0 auto",
//                         }}
//                     >
//                         <div
//                             style={{
//                                 width: "100%",
//                                 height: getHeight(stats.waitingApproval),
//                                 background: "#ffc107",
//                                 transition: "height 0.6s ease",
//                             }}
//                         ></div>
//                     </div>
//                     <p style={{ marginTop: "10px", fontWeight: "500" }}>
//                         결재 대기<br />
//                         <span style={{ color: "#ffc107", fontWeight: "bold" }}>
//                             {stats.waitingApproval}건
//                         </span>
//                     </p>
//                 </div>
//
//                 {/* 결재 완료 */}
//                 <div style={{ textAlign: "center" }}>
//                     <div
//                         style={{
//                             width: "70px",
//                             height: "150px",
//                             borderRadius: "35px",
//                             background: "#f1f3f5",
//                             overflow: "hidden",
//                             display: "flex",
//                             alignItems: "flex-end",
//                             margin: "0 auto",
//                         }}
//                     >
//                         <div
//                             style={{
//                                 width: "100%",
//                                 height: getHeight(stats.approved),
//                                 background: "#28a745",
//                                 transition: "height 0.6s ease",
//                             }}
//                         ></div>
//                     </div>
//                     <p style={{ marginTop: "10px", fontWeight: "500" }}>
//                         결재 완료<br />
//                         <span style={{ color: "#28a745", fontWeight: "bold" }}>
//                             {stats.approved}건
//                         </span>
//                     </p>
//                 </div>
//             </div>
//         </div>
//     );
// }
//
// export default Dashboard;
