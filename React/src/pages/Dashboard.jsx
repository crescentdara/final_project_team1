import {useEffect, useState} from "react";
import {getDashboardStats} from "../api";
import {useNavigate} from "react-router-dom";
import axios from "axios";

// ✅ 같은 폴더라서 상대경로는 이렇게
import MessageTabs from "./MessageTabs.jsx";

function Dashboard() {
    const navigate = useNavigate();
    const [stats, setStats] = useState(null);
    const [senderId, setSenderId] = useState(null); // 메시지 보낼 현재 사용자 ID
    const pct = (num, den) => (den ? ((num / den) * 100).toFixed(1) : "0.0");


    // 통계
    useEffect(() => {
        getDashboardStats()
            .then((data) => setStats(data))
            .catch((err) => console.error("통계 데이터 불러오기 실패:", err));
    }, []);

    // 현재 로그인 사용자 ID 조회 (메시지 전송에 필요)
    useEffect(() => {
        // 이미 상위에서 주입해둔 전역이 있으면 먼저 사용
        const preset = window.__USER?.userId;
        if (preset) {
            setSenderId(preset);
            return;
        }
        axios
            .get("/web/api/auth/me", {withCredentials: true})
            .then((res) => setSenderId(res.data?.userId ?? null))
            .catch((e) => console.error("현재 사용자 조회 실패:", e));
    }, []);

    if (!stats) return <p>로딩 중...</p>;

    // ✅ 원본 값 그대로 사용
    const totalBuildings = stats.totalBuildings;

    // 색상
    const SKY = "#c2dbff";     // 캡슐 전체(배경)
    const ORANGE = "#f18257";  // 캡슐 진행/확정
    const YELLOW = "#ffdc38";
    const GREEN = "#3bc894";   // 캡슐 진행/확정
    const PROG_A = "#98c3f1";  // 진행률 바 (민트)
    const PROG_B = "#5993ec";  // 진행률 바 그라디언트

    // 높이/표시
    const getHeight = (v) => (!totalBuildings ? "0%" : `${(v / totalBuildings) * 100}%`);
    const getHeightBy = (v, b) => (!b ? "0%" : `${(v / b) * 100}%`);
    const compact = (v) => `${v}/${totalBuildings}`;
    const compactBy = (v, b) => `${v}/${b || 0}`;

    // 진행률 퍼센트 (0~100)
    const progressPct = Math.max(0, Math.min(100, Number(stats.progressRate)));

    return (
        // ✅ 대시보드 전체를 2열 그리드로 분할: 좌(통계) / 우(메시지)
        <section
            style={{
                width: "100%",
                height: "100%",
                margin: "16px auto",
                display: "grid",
                gridTemplateColumns: "1fr 1fr",
                gap: 20,
                alignItems: "center",
            }}
        >
            {/* ───────── 좌측 : 기존 통계 카드 ───────── */}
            <div
                style={{
                    padding: 28,
                    borderRadius: 14,
                    background: "#fff",
                    boxShadow: "0 10px 30px rgba(16,24,40,0.06)",
                    display: "grid",
                    minWidth: 400,
                    height: "100%",
                }}
            >
                {/* 왼쪽: 총 조사 진행률 - 가로 막대 */}
                <div style={{display: "flex", flexDirection: "column", justifyContent: "center",}}>
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
                            alignContent: "center",
                            marginTop: 0,
                            marginBottom: "auto",
                        }}
                    >
                        전체 통계
                    </div>


                    <div style={{
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "center",
                        justifyContent: "center",
                        gap: 20
                    }}>

                        <div style={{color: "#1d55ac", fontSize: 20, fontWeight: 600}}>
                            총 조사 진행률
                        </div>
                        {/* 큰 진행률 바 */}
                        <div
                            style={{
                                position: "relative",
                                width: "80%",
                                minWidth: "250px",
                                height: 70,
                                background: "#eef2ff",
                                borderRadius: 999,
                                overflow: "hidden",
                                boxShadow: "inset 0 1px 0 rgba(255,255,255,.7)",
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

                        {/* 범례 */}
                        <div style={{
                            display: "flex",
                            gap: 16,
                            alignItems: "center",
                            flexWrap: "wrap",
                            marginBottom: 25
                        }}>
                            <div style={{display: "flex", alignItems: "center", gap: 8}}>
                            <span style={{
                                width: 14,
                                height: 14,
                                borderRadius: 4,
                                background: SKY,
                                border: "1px solid #e2e8f0"
                            }}/>
                                <span style={{fontSize: 12, color: "#334155", fontWeight: 700}}> : 전체</span>
                            </div>
                            <div style={{display: "flex", alignItems: "center", gap: 8}}>
                            <span style={{
                                width: 14,
                                height: 14,
                                borderRadius: 4,
                                background: ORANGE,
                                border: "1px solid #e2e8f0"
                            }}/>
                                <span style={{fontSize: 12, color: "#334155", fontWeight: 700}}> : 배정</span>
                            </div>
                            <div style={{display: "flex", alignItems: "center", gap: 8}}>
                            <span style={{
                                width: 14,
                                height: 14,
                                borderRadius: 4,
                                background: YELLOW,
                                border: "1px solid #e2e8f0"
                            }}/>
                                <span style={{fontSize: 12, color: "#334155", fontWeight: 700}}> : 대기</span>
                            </div>
                            <div style={{display: "flex", alignItems: "center", gap: 8}}>
                            <span style={{
                                width: 14,
                                height: 14,
                                borderRadius: 4,
                                background: GREEN,
                                border: "1px solid #e2e8f0"
                            }}/>
                                <span style={{fontSize: 12, color: "#334155", fontWeight: 700}}> : 완료</span>
                            </div>
                        </div>


                    </div>
                    <div>
                        {/* 오른쪽: 캡슐 3개 */}
                        <div
                            style={{
                                display: "flex",
                                justifyContent: "center",
                                alignItems: "flex-end",
                                minHeight: 320,
                                gap: 20,
                            }}
                        >
                            {/* 배정률 */}
                            <div style={{textAlign: "center"}}>
                                <div style={{ marginBottom: 6, fontWeight: 800, color: "#1d55ac" }}>
                                    {pct(stats.assignedBuildings, totalBuildings)}%
                                </div>
                                <div
                                    style={{
                                        width: 100,
                                        height: 400,
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
                                <div style={{marginTop: 10, fontWeight: 800, fontSize: 14}}>배정률</div>
                                <div style={{
                                    fontWeight: 700,
                                    fontSize: 12,
                                    color: ORANGE
                                }}>{compact(stats.assignedBuildings)}</div>
                            </div>

                            {/* 결재대기중 */}
                            <div style={{textAlign: "center"}}>
                                <div style={{ marginBottom: 6, fontWeight: 800, color: "#1d55ac" }}>
                                    {pct(stats.waitingApproval, stats.assignedBuildings)}%
                                </div>
                                <div
                                    style={{
                                        width: 100,
                                        height: 400,
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
                                <div style={{marginTop: 10, fontWeight: 800, fontSize: 14}}>결재대기중</div>
                                <div style={{fontWeight: 700, fontSize: 12, color: YELLOW}}>
                                    {compactBy(stats.waitingApproval, stats.assignedBuildings)}
                                </div>
                            </div>

                            {/* 결재완료 */}
                            <div style={{textAlign: "center"}}>
                                <div style={{ marginBottom: 6, fontWeight: 800, color: "#1d55ac" }}>
                                    {pct(stats.approved, stats.assignedBuildings)}%
                                </div>
                                <div
                                    style={{
                                        width: 100,
                                        height: 400,
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
                                <div style={{marginTop: 10, fontWeight: 800, fontSize: 14}}>결재완료</div>
                                <div style={{fontWeight: 700, fontSize: 12, color: GREEN}}>
                                    {compactBy(stats.approved, stats.assignedBuildings)}
                                </div>
                            </div>
                        </div>
                    </div>
                    <div style={{textAlign: "end", marginBottom: 0, marginTop: "auto",}}>
                        <button
                            onClick={() => navigate("/approvals")}
                            style={{
                                width: "130px",
                                height: "35px",
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
            </div>

            {/* ───────── 우측 : 메시지 탭 ───────── */}
            <div
                style={{
                    padding: 16,
                    borderRadius: 14,
                    background: "#fff",
                    boxShadow: "0 10px 30px rgba(16,24,40,0.06)",
                    minWidth: 400,
                    height: "100%",
                    overflow: "auto",
                }}
            >
                <MessageTabs senderId={senderId}/>
            </div>
        </section>
    );
}

export default Dashboard;
