// src/components/users/UserSearchBar.jsx
import { useEffect, useMemo, useState } from "react";
import { fetchUsers } from "../../api/users";

export default function UserSearchBar({ value, onChange }) {
  const [keyword, setKeyword] = useState("");
  const [options, setOptions] = useState([]); // 초기값 배열

  useEffect(() => {
    let alive = true;
    (async () => {
      try {
        const list = await fetchUsers(keyword);
        if (alive) setOptions(Array.isArray(list) ? list : []); // ✅ 한번 더 가드
      } catch (e) {
        console.error("fetchUsers error:", e);
        if (alive) setOptions([]); // 실패 시에도 배열
      }
    })();
    return () => { alive = false; };
  }, [keyword]);

  const opts = useMemo(() => {
    const list = Array.isArray(options) ? options : [];          // ✅ map 가드
    return list.map(u => ({
      id: u.userId,
      label: `${u.name || u.username} (ID:${u.userId})`,
    }));
  }, [options]);

  const selectedId = value ?? "";

  return (
      <div className="d-flex gap-2">
        <select
            className="form-select"
            style={{ maxWidth: 280 }}
            value={selectedId}
            onChange={(e) => onChange(e.target.value ? Number(e.target.value) : null)}
        >
          <option value="">- 조사원명 -</option>
          {opts.map(o => <option key={o.id} value={o.id}>{o.label}</option>)}
        </select>

        <input
            className="form-control"
            placeholder="이름/아이디 검색"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
        />
      </div>
  );
}
