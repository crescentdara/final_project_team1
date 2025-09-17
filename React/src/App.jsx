import { Routes, Route, Link } from 'react-router-dom';
import SurveyList from './pages/SurveyList.jsx';
import CreateSurvey from "./pages/CreateSurvey.jsx";

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

                            <Link to="/CreateSurvey" className="btn btn-primary mt-3">
                                CreateSurvey 페이지로 이동
                            </Link>

                            <Link to="/surveyList" className="btn btn-primary mt-3">
                                SurveyList 페이지로 이동
                            </Link>
                        </div>
                    }
                />

                {/* SurveyList 페이지 */}
                <Route path="/surveyList" element={<SurveyList />} />

                {/* CreateSurvey 페이지 */}
                <Route path="/createSurvey" element={<CreateSurvey/>} />
            </Routes>
        </div>
    );
}

export default App;
