// src/components/users/AssignmentList.jsx
export default function AssignmentList({ loading, items = [] }) {
  return (
      <div className="mt-4">
        {/*<h6 className="mb-2">배정 건물 목록</h6>*/}
        <div className="table-responsive">
          <table className="table table-sm table-striped">
            <thead className="table-light">
            <tr className="text-center">
              <th style={{width:80}}>ID</th>
              <th>지번주소</th></tr>
            </thead>
            <tbody>
            {loading ? (
                <tr><td colSpan={2}>불러오는 중…</td></tr>
            ) : items.length ? (
                items.map(it => (
                    <tr className="text-center" key={it.buildingId}>
                      <td>{it.buildingId}</td>
                      <td>{it.lotAddress}</td>
                    </tr>
                ))
            ) : (
                <tr><td colSpan={2} className="text-muted">배정된 건물이 없습니다.</td></tr>
            )}
            </tbody>
          </table>
        </div>
      </div>
  );
}
