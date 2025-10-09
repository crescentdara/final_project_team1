import {useEffect, useState} from "react";
import {Navigate, Route, Routes} from "react-router-dom";
import axios from "axios";

import MainLayout from "./components/ui/MainLayout.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import SurveyList from "./pages/SurveyList.jsx";
import CreateUser from "./pages/CreateUser.jsx";
import ApprovalFilters from "./pages/ApprovalFilters.jsx";
import ResultReport from "./pages/ResultReport.jsx";
import UserDetail from "./pages/UserDetail.jsx";
import Login from "./pages/Login.jsx";
import SurveyIndex from "./pages/SurveyIndex.jsx";
import ApproverAssignment from "./pages/ApproverAssignment.jsx";
import SurveyRegister from "./pages/SurveyRegister.jsx";

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
        <Routes>
            {/* 공통 레이아웃 */}
            <Route element={<MainLayout user={user} onLogout={handleLogout} />}>
                <Route path="/" element={<Dashboard/>} />

                <Route path="/surveyList" element={<SurveyList />} />

                {/* 기존 단건/다건 라우트 제거 */}
                {/*<Route path="/createSurvey" element={<CreateSurvey />} />*/}
                {/*<Route path="/buildingUpload" element={<BuildingUpload />} />*/}

                {/*단건/다건 통합 라우트*/}
                <Route path="/surveyRegister" element={<SurveyRegister />} />
                <Route path="/createSurvey" element={<Navigate to="/surveyRegister?tab=single" replace />} />
                <Route path="/buildingUpload" element={<Navigate to="/surveyRegister?tab=bulk" replace />} />


                <Route path="/createUser" element={<CreateUser />} />
                {/*<Route path="/" element={<Dashboard senderId={user?.id} />} />*/}
                {/*<Route path="/dashboard" element={<Dashboard senderId={user?.id} />} />*/}
                <Route path="/" element={<Dashboard senderId={(user?.userId ?? user?.id) ?? null} />} />
                <Route path="/dashboard" element={<Dashboard senderId={(user?.userId ?? user?.id) ?? null} />} />
                <Route path="/resultReport" element={<ResultReport />} />
                <Route path="/users" element={<UserDetail />} />
                <Route path="/approvals" element={<ApprovalFilters />} />
                <Route path="/approverAssignment" element={<ApproverAssignment/>} />
                {/*<Route path="/messageTabs" element={<MessageTabs senderId={user?.id} />} />*/}
                <Route path="/surveyIndex" element={<SurveyIndex />} />

                {/* 권한 예시 */}
                <Route path="/admin-only" element={user?.role === "ADMIN" ? <Dashboard /> : <Navigate to="/login" />}  senderId={user?.id}/>
                <Route path="/login" element={<Login onLogin={handleLogin} />} />
            </Route>
        </Routes>
    );
}

export default App;
