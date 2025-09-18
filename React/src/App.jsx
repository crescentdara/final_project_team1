import { Routes, Route, Link } from "react-router-dom";
import SurveyList from "./pages/SurveyList.jsx";
import CreateSurvey from "./pages/CreateSurvey.jsx";
import CreateUser from "./pages/CreateUser.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import ResultReport from "./pages/ResultReport.jsx"; // ✅ 추가

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
            </Routes>
        </div>
    );
}

export default App;
