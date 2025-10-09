import { useEffect, useState } from "react";
import { getDashboardStats } from "../api";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { Tabs, Tab } from "react-bootstrap";
import MessageSend from "./MessageSend.jsx";
import MessageSent from "./MessageSent.jsx";

function Dashboard({ senderId }) {
    const navigate = useNavigate();

    // 대시보드 통계
    const [stats, setStats] = useState(null);

    // 메시지용 senderId (prop 우선, 없으면 /auth/me)
    const [selfSenderId, setSelfSenderId] = useState(null);

    // 메시지 탭 내부 상태(보낸 직후 “보낸함” 즉시 반영)
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState(null);

    // 퍼센트 표기 유틸
    const pct = (num, den) => (den ? ((num / den) * 100).toFixed(1) : "0.0");

    // 통계 로드
    useEffect(() => {
        getDashboardStats()
            .then((data) => setStats(data))
            .catch((err) => console.error("통계 데이터 불러오기 실패:", err));
    }, []);

    // 현재 사용자 조회 (prop 없을 때만)
    useEffect(() => {
        if (senderId) return;
        const preset = window.__USER?.userId ?? window.__USER?.id;
        if (preset) {
            setSelfSenderId(preset);
            return;
        }
        axios.get("/web/api/auth/me", { withCredentials: true })
           .then((res) => setSelfSenderId(res.data?.userId ?? res.data?.id ?? null))
            .catch((e) => console.error("현재 사용자 조회 실패:", e));
    }, [senderId]);

// 안전한 합성값


    if (!stats) return <p>로딩 중...</p>;

    // ===== 그래프 계산 =====
    const totalBuildings = stats.totalBuildings;
    const SKY = "#c2dbff";
    const ORANGE = "#f18257";
    const YELLOW = "#ffdc38";
    const GREEN = "#3bc894";
    const PROG_A = "#98c3f1";
    const PROG_B = "#5993ec";

    const getHeight = (v) => (!totalBuildings ? "0%" : `${(v / totalBuildings) * 100}%`);
    const getHeightBy = (v, b) => (!b ? "0%" : `${(v / b) * 100}%`);
    const compact = (v) => `${v}/${totalBuildings}`;
    const compactBy = (v, b) => `${v}/${b || 0}`;
    const progressPct = Math.max(0, Math.min(100, Number(stats.progressRate)));

    const effectiveSenderId = senderId ?? selfSenderId;

    return (
        // 좌(통계) / 우(메시지) 1:1
        <section
            style={{
                width: "100%",
                height: "90vh",
                margin: "16px auto",
                display: "grid",
                gridTemplateColumns: "1fr 1fr",
                gap: 20,
                alignItems: "start",
            }}
        >
            {/* ===== 좌측: 대시보드 카드 ===== */}
            <div
                style={{
                    padding: 28,
                    borderRadius: 14,
                    background: "#fff",
                    boxShadow: "0 10px 30px rgba(16,24,40,0.06)",
                    minWidth: 400,
                    height: "87.5vh",
                    display: "flex",
                    flexDirection: "column",
                }}
            >
                {/* 상단 타이틀 */}
                <div
                    style={{
                        display: "inline-block",
                        width: 150,
                        background: "black",
                        color: "#fff",
                        padding: "6px 12px",
                        borderRadius: 5,
                        textAlign: "center",
                        fontSize: 20,
                        fontWeight: 800,
                        letterSpacing: 0.3,
                    }}
                >
                    전체 통계
                </div>

                {/* 총 진행률 */}
                <div
                    style={{
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "center",
                        gap: 20,
                        marginTop: 10,
                    }}
                >
                    <div style={{ color: "#1d55ac", fontSize: 20, fontWeight: 600 }}>총 조사 진행률</div>

                    {/* 큰 진행률 바 */}
                    <div
                        style={{
                            position: "relative",
                            width: "80%",
                            minWidth: 250,
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
                    <div
                        style={{
                            display: "flex",
                            gap: 16,
                            alignItems: "center",
                            flexWrap: "wrap",
                            marginBottom: 25,
                        }}
                    >
                        <Legend color={SKY} label=": 전체" />
                        <Legend color={ORANGE} label=": 배정" />
                        <Legend color={YELLOW} label=": 대기" />
                        <Legend color={GREEN} label=": 완료" />
                    </div>
                </div>

                {/* 세로 캡슐 3개 */}
                <div
                    style={{
                        display: "flex",
                        justifyContent: "center",
                        alignItems: "flex-end",
                        gap: 20,
                        marginTop: "auto",
                        marginBottom: 10,
                    }}
                >
                    <BarCapsule
                        title="배정률"
                        valuePct={pct(stats.assignedBuildings, totalBuildings)}
                        heightPct={getHeight(stats.assignedBuildings)}
                        colorFill={ORANGE}
                        colorBg={SKY}
                        compactText={compact(stats.assignedBuildings)}
                    />
                    <BarCapsule
                        title="결재대기중"
                        valuePct={pct(stats.waitingApproval, stats.assignedBuildings)}
                        heightPct={getHeightBy(stats.waitingApproval, stats.assignedBuildings)}
                        colorFill={YELLOW}
                        colorBg={SKY}
                        compactText={compactBy(stats.waitingApproval, stats.assignedBuildings)}
                    />
                    <BarCapsule
                        title="결재완료"
                        valuePct={pct(stats.approved, stats.assignedBuildings)}
                        heightPct={getHeightBy(stats.approved, stats.assignedBuildings)}
                        colorFill={GREEN}
                        colorBg={SKY}
                        compactText={compactBy(stats.approved, stats.assignedBuildings)}
                    />
                </div>

                {/* 버튼 */}
                <div style={{ textAlign: "end" }}>
                    <button
                        onClick={() => navigate("/approvals")}
                        style={{
                            width: 130,
                            height: 35,
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

            {/* ===== 우측: 메시지(탭) ===== */}
            <div
                style={{
                    padding: 26,
                    borderRadius: 14,
                    background: "#fff",
                    boxShadow: "0 10px 30px rgba(16,24,40,0.06)",
                    minWidth: 400,
                    height: "87.5vh",
                    overflow: "auto",
                }}
            >
                <h3
                    className="fw-bold mt-2 mb-4"
                    style={{ borderLeft: "4px solid #6898FF", paddingLeft: 12 }}
                >
                    메시지 관리
                </h3>

                <Tabs defaultActiveKey="send" id="message-tabs" className="mb-3" fill>
                    <Tab eventKey="send" title="메시지 보내기">
                        <MessageSend
                            senderId={effectiveSenderId}
                            onMessageSent={(msg) => {
                                setMessages((prev) => [msg, ...prev]);
                                setNewMessage(msg);
                            }}
                        />
                    </Tab>
                    <Tab eventKey="sent" title="보낸 메시지함">
                        <MessageSent
                            senderId={effectiveSenderId}
                            // 아래 두 prop은 현재 MessageSent에서 사용하진 않지만,
                            // 원하면 메시지 상태를 외부로 뺄 때 재사용 가능
                            messages={messages}
                            setMessages={setMessages}
                            newMessage={newMessage}
                        />
                    </Tab>
                </Tabs>
            </div>
        </section>
    );
}

/* ───────────────── Helper Components ───────────────── */

function Legend({ color, label }) {
    return (
        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
      <span
          style={{
              width: 14,
              height: 14,
              borderRadius: 4,
              background: color,
              border: "1px solid #e2e8f0",
          }}
      />
            <span style={{ fontSize: 12, color: "#334155", fontWeight: 700 }}>
        {label}
      </span>
        </div>
    );
}

function BarCapsule({ title, valuePct, heightPct, colorFill, colorBg, compactText }) {
    return (
        <div style={{ textAlign: "center" }}>
            <div style={{ marginBottom: 6, fontWeight: 800, color: "#1d55ac" }}>
                {valuePct}%
            </div>
            <div
                style={{
                    width: 100,
                    height: 350,
                    borderRadius: 24,
                    background: colorBg,
                    overflow: "hidden",
                    display: "flex",
                    alignItems: "flex-end",
                    boxShadow: "inset 0 1px 0 rgba(255,255,255,.6)",
                }}
            >
                <div
                    style={{
                        width: "100%",
                        height: heightPct,
                        background: colorFill,
                        transition: "height .6s ease",
                    }}
                />
            </div>
            <div style={{ marginTop: 10, fontWeight: 800, fontSize: 14 }}>{title}</div>
            <div style={{ fontWeight: 700, fontSize: 12, color: colorFill }}>{compactText}</div>
        </div>
    );
}

export default Dashboard;
