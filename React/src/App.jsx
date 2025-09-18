import { Routes, Route, Link } from 'react-router-dom';
import SurveyList from './pages/SurveyList.jsx';
import UserDetail from "./pages/UserDetail.jsx";
import ApprovalFilters from "./pages/ApprovalFilters.jsx";
import TotalSurveyList from "./pages/TotalSurveyList.jsx";

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
                  <Link to="/web/surveyList" className="btn btn-primary me-3">SurveyList 페이지로 이동</Link>
                  <Link to="/web/api/users" className={'btn btn-outline-secondary me-3'}>조사원 상새정보</Link>
                  <Link to="/web/api/approval" className={'btn btn-outline-primary me-3'}>결재 대기 중</Link>
                  <Link to="/web/api/survey" className={'btn btn-outline-info'}>전체 조사 목록</Link>
                </div>
              }
          />

          {/* SurveyList 페이지 */}
          <Route path="/web/surveyList" element={<SurveyList />} />

          {/* 조사원 상세정보 */}
          <Route path="/web/api/users" element={<UserDetail />} />

          {/* 결재 대기 중 페이지 */}
          <Route path="/web/api/approval" element={<ApprovalFilters />} />

          {/* 전체 조사 목록 페이지 */}
          <Route path="/web/api/survey" element={<TotalSurveyList />} />
        </Routes>
      </div>
  );
}

export default App;
