// src/components/users/UserSearchBar.jsx
import { useEffect, useMemo, useState } from "react";
import { fetchUsersAdvanced } from "../../api/users";

export default function UserSearchBar({ value, onChange }) {
  // 'all' | 'username' | 'name' | 'role'
  const [field, setField] = useState("all");
  const [keyword, setKeyword] = useState("");
  const [options, setOptions] = useState([]);
  const [loading, setLoading] = useState(false);

  async function runSearch(autoSelect = true) {
    setLoading(true);
    try {
      const list = await fetchUsersAdvanced({ field, keyword });
      setOptions(Array.isArray(list) ? list : []);
      if (autoSelect && list?.length) {
        onChange(list[0].userId); // ✅ 첫 결과 자동 선택 → 디테일 자동 표시
      }
    } catch (e) {
      console.error("search error:", e);
      setOptions([]);
    } finally {
      setLoading(false);
    }
  }

  // 최초 로드 시 전체 목록
  useEffect(() => {
    runSearch(false);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const selectedId = value ?? "";
  const opts = useMemo(
      () =>
          (Array.isArray(options) ? options : []).map((u) => ({
            id: u.userId,
            label: `${u.name || u.username} ( ID : ${u.username}${u.role ? ` / ${u.role} ` : ""})`,
          })),
      [options]
  );

  // placeholder 동적 변경
  const placeholder =
      field === "name"
      ? "이름 검색"
      : field === "username"
      ? "ID 검색"
      : field === "role"
      ? "역할 입력 ( ADMIN / EDITOR / VIEWER )"
      : "통합 검색";

  return (
      <div className="d-flex flex-wrap gap-2">
        {/* 조사원 선택 드롭다운 (검색 결과 목록) */}
        <select
            className="form-select"
            style={{ maxWidth: 300, minWidth: 220 }}
            value={selectedId}
            onChange={(e) =>
                onChange(e.target.value ? Number(e.target.value) : null)
            }
            disabled={loading}
        >
          <option value="">{loading ? "불러오는 중…" : "- 이름 ( ID / 역할 ) -"}</option>
          {opts.map((o) => (
              <option key={o.id} value={o.id}>
                {o.label}
              </option>
          ))}
        </select>

        {/* 검색 옵션 */}
        <select
            className="form-select"
            style={{ maxWidth: 140 }}
            value={field}
            onChange={(e) => {
              setField(e.target.value);
              setKeyword("");
            }}
        >
          <option value="all">전체</option>
          <option value="name">이름</option>
          <option value="username">ID</option>
          <option value="role">역할</option>
        </select>

        {/* 검색어 입력 */}
        <input
            className="form-control"
            style={{ minWidth: 260, maxWidth: 420 }}
            placeholder={placeholder}
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && runSearch(true)}
        />

        <button className="btn btn-secondary" onClick={() => runSearch(true)} disabled={loading}>
          검색
        </button>
      </div>
  );
}
