// src/pages/UserDetail.jsx
import { useEffect, useState } from "react";
import { fetchUserDetail, fetchUserAssignments } from "../api/users";
import UserSearchBar from "../components/users/UserSearchBar";
import UserDetailCard from "../components/users/UserDetailCard";
import AssignmentList from "../components/users/AssignmentList";

export default function UserDetail() {
  const [selectedUserId, setSelectedUserId] = useState(null);
  const [detail, setDetail] = useState(null);
  const [assignments, setAssignments] = useState([]);

  const [loadingDetail, setLoadingDetail] = useState(false);
  const [loadingAssign, setLoadingAssign] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!selectedUserId) { setDetail(null); setAssignments([]); return; }

    let alive = true;
    (async () => {
      try {
        setLoadingDetail(true); setError(null);
        const d = await fetchUserDetail(selectedUserId);
        if (alive) setDetail(d);
      } catch (e) {
        if (alive) setError(e);
      } finally {
        if (alive) setLoadingDetail(false);
      }
    })();

    (async () => {
      try {
        setLoadingAssign(true);
        const list = await fetchUserAssignments(selectedUserId);
        if (alive) setAssignments(list);
      } finally {
        if (alive) setLoadingAssign(false);
      }
    })();

    return () => { alive = false; };
  }, [selectedUserId]);

  return (
      <div className="container mt-4">
        <h3 className="mb-3">조사원 상세정보 조회</h3>

        <UserSearchBar value={selectedUserId} onChange={setSelectedUserId} />

        {error && (
            <div className="alert alert-danger mt-3">
              데이터를 불러오지 못했습니다. {String(error.message || error)}
            </div>
        )}

        <UserDetailCard loading={loadingDetail} detail={detail} />
        <AssignmentList loading={loadingAssign} items={assignments} />
      </div>
  );
}
