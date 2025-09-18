import { useEffect, useState } from "react";
import axios from "axios";

function SurveyPending() {
    const [surveys, setSurveys] = useState([]);
    const [selectedSurvey, setSelectedSurvey] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);

    useEffect(() => {
        axios
            .get("/web/api/surveys")
            .then((res) => setSurveys(res.data))
            .catch((err) => console.error("❌ 목록 불러오기 실패:", err));
    }, []);

    const handleView = (id) => {
        axios
            .get(`/web/api/surveys/${id}`)
            .then((res) => {
                setSelectedSurvey(res.data);
                setIsModalOpen(true);
            })
            .catch((err) => console.error("❌ 상세 불러오기 실패:", err));
    };

    // 📌 승인 처리
    const handleApprove = () => {
        if (!selectedSurvey) return;

        axios
            .post(`/web/report/approve/${selectedSurvey.id}?userId=1`) // 임시: 승인자 userId=1
            .then(() => {
                alert("승인되었습니다!");
                setIsModalOpen(false);

                // 승인 후 목록 갱신 (새로 불러오기)
                return axios.get("/web/api/surveys");
            })
            .then((res) => setSurveys(res.data))
            .catch((err) => {
                console.error("❌ 승인 실패:", err);
                alert("승인 실패");
            });
    };

    // 📌 재조사 처리 (아직 API 미구현 → 자리만 잡기)
    const handleReSurvey = () => {
        alert("재조사 처리 API 준비 중!");
    };

    return (
        <div>
            <h2>결재 대기 목록</h2>
            <table border="1">
                <thead>
                <tr>
                    <th>건물명</th>
                    <th>조사자</th>
                    <th>상태</th>
                    <th>보기</th>
                </tr>
                </thead>
                <tbody>
                {surveys.map((s) => (
                    <tr key={s.id}>
                        <td>{s.buildingName}</td>
                        <td>{s.userName}</td>
                        <td>{s.status}</td>
                        <td>
                            <button onClick={() => handleView(s.id)}>보기</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>

            {/* 📌 모달 */}
            {isModalOpen && selectedSurvey && (
                <div
                    style={{
                        position: "fixed",
                        top: "20%",
                        left: "50%",
                        transform: "translateX(-50%)",
                        background: "#fff",
                        padding: "20px",
                        border: "1px solid #ccc",
                        zIndex: 1000,
                        width: "600px",
                        maxHeight: "80vh",
                        overflowY: "auto",
                    }}
                >
                    <h3>조사 상세 내역</h3>
                    <table border="1" cellPadding="5" style={{ borderCollapse: "collapse", width: "100%" }}>
                        <tbody>
                        <tr><th>건물명</th><td>{selectedSurvey.buildingName}</td></tr>
                        <tr><th>조사자</th><td>{selectedSurvey.userName}</td></tr>
                        <tr><th>상태</th><td>{selectedSurvey.status}</td></tr>
                        <tr><th>조사불가 여부</th><td>{selectedSurvey.possible}</td></tr>
                        <tr><th>행정목적 활용 여부</th><td>{selectedSurvey.adminUse}</td></tr>
                        <tr><th>유휴비율</th><td>{selectedSurvey.idleRate}</td></tr>
                        <tr><th>안전등급</th><td>{selectedSurvey.safety}</td></tr>
                        <tr><th>외부상태 - 외벽</th><td>{selectedSurvey.wall}</td></tr>
                        <tr><th>외부상태 - 옥상</th><td>{selectedSurvey.roof}</td></tr>
                        <tr><th>외부상태 - 창호</th><td>{selectedSurvey.windowState}</td></tr>
                        <tr><th>외부상태 - 주차 가능 여부</th><td>{selectedSurvey.parking}</td></tr>
                        <tr><th>내부상태 - 현관</th><td>{selectedSurvey.entrance}</td></tr>
                        <tr><th>내부상태 - 천장</th><td>{selectedSurvey.ceiling}</td></tr>
                        <tr><th>내부상태 - 바닥</th><td>{selectedSurvey.floor}</td></tr>
                        <tr><th>외부 기타사항</th><td>{selectedSurvey.extEtc}</td></tr>
                        <tr><th>내부 기타사항</th><td>{selectedSurvey.intEtc}</td></tr>
                        <tr><th>외부사진</th><td>{selectedSurvey.extPhoto}</td></tr>
                        <tr><th>외부편집사진</th><td>{selectedSurvey.extEditPhoto}</td></tr>
                        <tr><th>내부사진</th><td>{selectedSurvey.intPhoto}</td></tr>
                        <tr><th>내부편집사진</th><td>{selectedSurvey.intEditPhoto}</td></tr>
                        </tbody>
                    </table>

                    <div style={{ marginTop: "20px", textAlign: "right" }}>
                        <button onClick={handleApprove} style={{ marginRight: "10px" }}>승인</button>
                        <button onClick={handleReSurvey} style={{ marginRight: "10px" }}>재조사</button>
                        <button onClick={() => setIsModalOpen(false)}>닫기</button>
                    </div>
                </div>
            )}
        </div>
    );
}

export default SurveyPending;
