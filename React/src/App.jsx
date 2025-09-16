import { Routes, Route, Link } from 'react-router-dom';
import SurveyList from './pages/SurveyList.jsx';
import UserDetail from "./pages/UserDetail.jsx";

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
                  <Link to="/surveyList" className="btn btn-primary me-3">
                    SurveyList 페이지로 이동
                  </Link>
                  <Link to="/users" className={'btn btn-outline-secondary'}>조사원 상새정보</Link>
                </div>
              }
          />

          {/* SurveyList 페이지 */}
          <Route path="/surveyList" element={<SurveyList />} />

          {/* 조사원 상세정보 */}
          <Route path="/users" element={<UserDetail />} />
        </Routes>
      </div>
  );
}

export default App;
