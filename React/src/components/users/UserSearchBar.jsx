import { useEffect, useMemo, useState } from "react";
import useDebounce from "../../hooks/useDebounce";
import { fetchUsers } from "../../api/users";

export default function UserSearchBar({ value, onChange }) {
  const [keyword, setKeyword] = useState("");
  const debounced = useDebounce(keyword, 300);
  const [options, setOptions] = useState([]);

  useEffect(() => {
    let alive = true;
    (async () => {
      const list = await fetchUsers(debounced);
      if (alive) setOptions(list || []);
    })();
    return () => { alive = false; };
  }, [debounced]);

  const selectedId = value ?? "";
  const opts = useMemo(
      () => options.map(u => ({ id: u.userId, label: `${u.name ?? u.username} (ID:${u.userId})` })),
      [options]
  );

  return (
      <div className="d-flex gap-2">
        <select
            className="form-select"
            style={{ maxWidth: 260 }}
            value={selectedId}
            onChange={(e) => onChange(e.target.value ? Number(e.target.value) : null)}
        >
          <option value="">- 조사원명 -</option>
          {opts.map(o => (
              <option key={o.id} value={o.id}>{o.label}</option>
          ))}
        </select>

        <div className="flex-grow-1 position-relative">
          <input
              className="form-control"
              placeholder="검색"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
          />
          <i
              className="bi bi-search"
              style={{
                position: "absolute", right: 10, top: "50%", transform: "translateY(-50%)",
                opacity: 0.6
              }}
          />
        </div>
      </div>
  );
}
