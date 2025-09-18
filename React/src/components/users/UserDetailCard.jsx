// src/components/users/UserDetailCard.jsx
export default function UserDetailCard({ loading, detail }) {
  return (
      <div className="table-responsive mt-3">
        <table className="table table-bordered align-middle">
          <thead className="table-light">
          <tr><th style={{width:220}}>조사원명</th><th>상세내용</th></tr>
          </thead>
          <tbody>
          {loading ? (
              <>
                <tr><td colSpan={2}>불러오는 중…</td></tr>
              </>
          ) : detail ? (
              <>
                <tr><td>이름</td><td>{detail.name}</td></tr>
                <tr><td>아이디</td><td>{detail.username}</td></tr>
                <tr><td>역할</td><td>{detail.role}</td></tr>
                <tr><td>상태</td><td>{String(detail.status)}</td></tr>
                <tr><td>생성일</td><td>{detail.createdAt}</td></tr>
              </>
          ) : (
              <tr><td colSpan={2} className="text-muted">
                상단에서 조사원을 선택하거나 검색하세요.
              </td></tr>
          )}
          </tbody>
        </table>
      </div>
  );
}
