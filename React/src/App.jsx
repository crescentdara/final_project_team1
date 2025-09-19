import { Routes, Route, Link } from "react-router-dom";
import SurveyList from "./pages/SurveyList.jsx";
import CreateSurvey from "./pages/CreateSurvey.jsx";
import CreateUser from "./pages/CreateUser.jsx";
import ApprovalFilters from "./pages/ApprovalFilters.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import ResultReport from "./pages/ResultReport.jsx";
import UserDetail from "./pages/UserDetail.jsx";
import TotalSurveyList from "./pages/TotalSurveyList.jsx"; // ✅ 추가

function App() {
    return (
        <div className="container mt-5">
            <Routes>
                {/* 메인 페이지 */}
                <Route
                    path="/"
                    element={
                        <div>
                            <h2>메인 페이지</h2>

                            <Link to="/surveyList" className="btn btn-primary mt-3">
                                SurveyList 페이지로 이동
                            </Link>

                            <Link to="/createSurvey" className="btn btn-primary mt-3">
                                CreateSurvey 페이지로 이동
                            </Link>

                            <Link to="/createUser" className="btn btn-primary mt-3">
                                CreateUser 페이지로 이동
                            </Link>

                            <Link to="/dashboard" className="btn btn-primary mt-3">
                                Dashboard 페이지로 이동
                            </Link>

                            <Link to="/resultReport" className="btn btn-primary mt-3">
                                Report 페이지로 이동
                            </Link>

                            <Link to="/users" className={'btn btn-outline-secondary me-3'}>
                                조사원 상세정보
                            </Link>

                            <Link to="/approvals" className={'btn btn-outline-primary me-3'}>
                                결재 대기 중
                            </Link>

                            <Link to="/survey" className={'btn btn-outline-info'}>
                                전체 조사 목록
                            </Link>

                            <Link to="/resultReport" className={'btn btn-outline-info'}>
                                resultReport 페이지로 이동
                            </Link>
                        </div>
                    }
                />

                {/* SurveyList 페이지 */}
                <Route path="/surveyList" element={<SurveyList />} />

                {/* CreateSurvey 페이지 */}
                <Route path="/createSurvey" element={<CreateSurvey />} />

                {/* CreateUser 페이지 */}
                <Route path="/createUser" element={<CreateUser />} />

                {/* Dashboard 페이지 */}
                <Route path="/dashboard" element={<Dashboard />} />

                {/* Dashboard 페이지 */}
                <Route path="/resultReport" element={<ResultReport />} />

                {/* 조사원 상세정보 */}
                <Route path="/users" element={<UserDetail />} />

                {/* 결재 대기 중 페이지 */}
                <Route path="/approvals" element={<ApprovalFilters />} />

                {/* 전체 조사 목록 페이지 */}
                <Route path="/survey" element={<TotalSurveyList />} />

                {/* 전체 조사 목록 페이지 */}
                <Route path="/resultReport" element={<ResultReport />} />
            </Routes>
        </div>
    );
}
export default App;
