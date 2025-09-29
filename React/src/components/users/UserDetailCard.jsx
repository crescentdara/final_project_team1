// ✅ 상태코드 → 라벨 매핑
const statusToText = (v) => {
  const n = Number(v);
  if (n === 1) return "활성";
  if (n === 2) return "비활성";
  return v ?? "-";
};

// ✅ 날짜 → 'YYYY-MM-DD HH:mm' 포맷
const formatYMDHM = (input) => {
  if (!input) return "-";
  if (typeof input === "string" && /^\d{4}-\d{2}-\d{2}/.test(input)) {
    // "2025-09-29T12:34:56..." 형태면 앞 16자리만
    return input.replace("T", " ").slice(0, 16);
  }
  const d = new Date(input);
  if (isNaN(d.getTime())) {
    // 파싱 실패 시에도 안전하게 자르기
    return String(input).replace("T", " ").slice(0, 16);
  }
  const pad = (x) => String(x).padStart(2, "0");
  const y = d.getFullYear();
  const m = pad(d.getMonth() + 1);
  const day = pad(d.getDate());
  const h = pad(d.getHours());
  const mi = pad(d.getMinutes());
  return `${y}-${m}-${day} ${h}:${mi}`;
};
export default function UserDetailCard({ loading, detail }) {
// 보기용 값 계산 (원본 필드명 변화에도 유연하게 대응)
  const statusDisplay = statusToText(detail?.status ?? detail?.statusCode);
  const createdDisplay = formatYMDHM(
      detail?.createdAt ?? detail?.createAt ?? detail?.createDate ?? detail?.regDate
  );

  return (
    <div className="table-responsive mt-3">
      <table className="table table-bordered align-middle">
        <thead className="table-light">
        <tr>
          <th style={{ width: 220 }}>조사원명</th>
          <th>상세내용</th>
        </tr>
        </thead>
        <tbody>
        {loading ? (
            <>
              <tr>
                <td colSpan={2}>불러오는 중…</td>
              </tr>
            </>
        ) : detail ? (
            <>
              <tr>
                <td>이름</td>
                <td>{detail.name}</td>
              </tr>
              <tr>
                <td>아이디</td>
                <td>{detail.username}</td>
              </tr>
              <tr>
                <td>역할</td>
                <td>{detail.role}</td>
              </tr>
              <tr>
                <td>상태</td>
                <td>{statusDisplay}</td> {/* ✅ 1→활성, 2→비활성 */}
              </tr>
              <tr>
                <td>생성일</td>
                <td>{createdDisplay}</td> {/* ✅ YYYY-MM-DD HH:mm */}
              </tr>
            </>
        ) : (
            <tr>
              <td colSpan={2} className="text-muted">
                상단에서 조사원을 선택하거나 검색하세요.
              </td>
            </tr>
        )}
        </tbody>
      </table>
    </div>
  );
}
