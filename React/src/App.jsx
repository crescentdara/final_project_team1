import {useEffect, useState} from "react";
import { Routes, Route, Link, Navigate } from "react-router-dom";
import Header from "./components/ui/Header";

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
import Message from "./pages/MessageTabs.jsx";
import MessageTabs from "./pages/MessageTabs.jsx";
import axios from "axios";
import SurveyIndex from "./pages/SurveyIndex.jsx";

function App() {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true); // ✅ 초기 로딩 상태

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
        <>
            {/* ✅ 모든 페이지 공통 Header */}
            <Header user={user} onLogout={handleLogout} />

            <div className="container mt-5">
                <Routes>
                    {/* 메인 페이지 */}
                    <Route
                        path="/"
                        element={
                            <div>
                                <h2>메인 페이지</h2>
                                <Link to="/surveyList" className="btn btn-primary mt-3">SurveyList 페이지로 이동</Link>
                                <Link to="/createSurvey" className="btn btn-primary mt-3">CreateSurvey 페이지로 이동</Link>
                                <Link to="/createUser" className="btn btn-primary mt-3">CreateUser 페이지로 이동</Link>
                                <Link to="/dashboard" className="btn btn-primary mt-3">Dashboard 페이지로 이동</Link>
                                <Link to="/resultReport" className="btn btn-primary mt-3">Report 페이지로 이동</Link>
                                <Link to="/users" className="btn btn-outline-secondary me-3">조사원 상세정보</Link>
                                <Link to="/approvals" className="btn btn-outline-primary me-3">결재 대기 중</Link>
                                <Link to="/survey" className="btn btn-outline-info">전체 조사 목록</Link>
                                <Link to="/login" className="btn btn-outline-info">Login 페이지로 이동</Link>
                                <Link to="/buildingUpload" className="btn btn-primary mt-3">다건 등록 페이지로 이동</Link>
                                <Link to="/messageTabs" className="btn btn-primary mt-3">messageTabs 페이지로 이동</Link>

                                <Link to="/surveyIndex" className="btn btn-primary mt-3">surveyIndex 페이지로 이동</Link>
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
                    <Route path="/survey" element={<TotalSurveyList />} />

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
        </>
    );
}

export default App;
