import { useEffect, useState } from "react";
import { Routes, Route, Link, Navigate } from "react-router-dom";

import SurveyList from "./pages/SurveyList.jsx";
import CreateSurvey from "./pages/CreateSurvey.jsx";
import CreateUser from "./pages/CreateUser.jsx";
import ApprovalFilters from "./pages/ApprovalFilters.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import ResultReport from "./pages/ResultReport.jsx";
import UserDetail from "./pages/UserDetail.jsx";
import TotalSurveyList from "./pages/TotalSurveyList.jsx";
import Login from "./pages/Login.jsx";
import BuildingUpload from "./pages/BuildingUpload.jsx";
import MessageTabs from "./pages/MessageTabs.jsx";
import axios from "axios";
import SurveyIndex from "./pages/SurveyIndex.jsx";
import ApproverAssignment from "./pages/ApproverAssignment.jsx";

function App() {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true); // 초기 로딩 상태

    const handleLogin = (userInfo) => {
        setUser(userInfo); // 로그인 시 상태 저장
    };

    const handleLogout = () => {
        setUser(null); // 로그아웃 시 상태 비우기
    };

    // ✅ 앱 시작할 때 세션 확인
    useEffect(() => {
        axios.get("/web/api/auth/me", { withCredentials: true })
            .then((res) => {
                if (res.data) {
                    setUser(res.data); // 세션에 로그인 정보 있으면 복원
                }
            })
            .catch(() => {
                console.log("세션 없음");
            })
            .finally(() => setLoading(false));
    }, []);

    if (loading) return <div>로딩 중...</div>; // 세션 확인 끝나기 전까지 잠깐 표시

    return (
        <div className="container mt-5">
            <Routes>
                {/* 메인 페이지 */}
                <Route
                    path="/"
                    element={
                        <div>
                            <h2>메인 페이지</h2>
                            <Link to="/surveyList" className="btn btn-primary mt-3">미배정 조사지 목록</Link>
                            <Link to="/createSurvey" className="btn btn-primary mt-3 ms-2">조사지 생성</Link>
                            <Link to="/createUser" className="btn btn-primary mt-3 ms-2">조사원 생성</Link>
                            <Link to="/dashboard" className="btn btn-primary mt-3 ms-2">통계</Link>
                            <Link to="/resultReport" className="btn btn-primary mt-3 ms-2">결재 완료</Link>
                            <Link to="/users" className="btn btn-primary mt-3 ms-2">조사원 상세정보</Link>
                            <Link to="/approvals" className="btn btn-primary mt-3 ms-2">결재 대기 중</Link>
                            <Link to="/login" className="btn btn-primary mt-3 ms-2">로그인</Link>
                            <Link to="/buildingUpload" className="btn btn-primary mt-3 ms-2">다건 등록</Link>
                            <Link to="/messageTabs" className="btn btn-primary mt-3 ms-2">메시지 전송</Link>
                            <Link to="/surveyIndex" className="btn btn-primary mt-3 ms-2">전체 조사지 리스트</Link>
                            <Link to="/approverAssignment" className="btn btn-primary mt-3 ms-2">결재자 배정</Link>
                        </div>
                    }
                />

                {/* 페이지 라우트들 */}
                <Route path="/surveyList" element={<SurveyList />} />
                <Route path="/createSurvey" element={<CreateSurvey />} />
                <Route path="/createUser" element={<CreateUser />} />
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/resultReport" element={<ResultReport />} />
                <Route path="/users" element={<UserDetail />} />
                <Route path="/approvals" element={<ApprovalFilters />} />
                <Route path="/approverAssignment" element={<ApproverAssignment />} />

                {/* 로그인 페이지 */}
                <Route path="/login" element={<Login onLogin={handleLogin} />} />

                {/* 로그인 필요 페이지 예시 */}
                <Route
                    path="/admin-only"
                    element={user?.role === "ADMIN" ? <Dashboard /> : <Navigate to="/login" />}
                />

                {/* Excel */}
                <Route path="/buildingUpload" element={<BuildingUpload />} />

                <Route
                    path="/messageTabs"
                    element={<MessageTabs senderId={user?.id} />}
                />

                <Route path="/surveyIndex" element={<SurveyIndex />} />
            </Routes>
        </div>
    );
}

export default App;
