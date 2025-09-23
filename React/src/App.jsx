import { useState } from "react";
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

function App() {
    const [user, setUser] = useState(null);

    const handleLogin = (userInfo) => {
        setUser(userInfo); // 로그인 시 상태 저장
    };

    const handleLogout = () => {
        setUser(null); // 로그아웃 시 상태 비우기
    };

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
                                <Link to="/buildingUpload" className="btn btn-primary mt-3">buildingUpload 페이지로 이동</Link>
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

                    <Route path="/buildingUpload" element={<BuildingUpload />} />
                </Routes>
            </div>
        </>
    );
}

export default App;
