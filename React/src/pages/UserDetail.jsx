import { useEffect, useState } from "react";
import { fetchUserAssignments, fetchUserDetail } from "../api/users";
import UserSearchBar from "../components/users/UserSearchBar";
import UserDetailCard from "../components/users/UserDetailCard";
import AssignmentList from "../components/users/AssignmentList";

export default function UserDetail() {
  const [selectedUserId, setSelectedUserId] = useState(null);

  const [detailLoading, setDetailLoading] = useState(false);
  const [assignLoading, setAssignLoading] = useState(false);

  const [detail, setDetail] = useState(null);
  const [assignments, setAssignments] = useState([]);

  useEffect(() => {
    if (!selectedUserId) {
      setDetail(null);
      setAssignments([]);
      return;
    }

    let alive = true;

    (async () => {
      try {
        setDetailLoading(true);
        const d = await fetchUserDetail(selectedUserId);
        if (alive) setDetail(d);
      } finally {
        if (alive) setDetailLoading(false);
      }
    })();

    (async () => {
      try {
        setAssignLoading(true);
        const list = await fetchUserAssignments(selectedUserId);
        if (alive) setAssignments(list || []);
      } finally {
        if (alive) setAssignLoading(false);
      }
    })();

    return () => { alive = false; };
  }, [selectedUserId]);

  return (
      <div className="container mt-4">
        <h3 className="mb-3">조사원 상세정보 조회</h3>

        <UserSearchBar value={selectedUserId} onChange={setSelectedUserId} />

        <UserDetailCard loading={detailLoading} detail={detail} />

        <AssignmentList loading={assignLoading} items={assignments} />
      </div>
  );
}
