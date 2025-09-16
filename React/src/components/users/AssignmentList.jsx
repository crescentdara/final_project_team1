import ShimmerRow from "../ui/ShimmerRow";

export default function AssignmentList({ loading, items = [] }) {
  return (
      <div className="mt-3">
        <h6 className="mb-2">배정 건물 목록</h6>
        <div className="table-responsive">
          <table className="table table-sm table-striped">
            <thead className="table-light">
            <tr>
              <th style={{ width: 120 }}>Building ID</th>
              <th>지번주소</th>
            </tr>
            </thead>
            <tbody>
            {loading ? (
                <>
                  <tr><td colSpan={2}><ShimmerRow /></td></tr>
                  <tr><td colSpan={2}><ShimmerRow /></td></tr>
                  <tr><td colSpan={2}><ShimmerRow /></td></tr>
                </>
            ) : items.length ? (
                items.map(it => (
                    <tr key={it.buildingId}>
                      <td>{it.buildingId}</td>
                      <td>{it.lotAddress}</td>
                    </tr>
                ))
            ) : (
                <tr>
                  <td colSpan={2} className="text-muted">배정된 건물이 없습니다.</td>
                </tr>
            )}
            </tbody>
          </table>
        </div>
      </div>
  );
}
