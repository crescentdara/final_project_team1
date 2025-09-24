import {useEffect, useMemo, useState} from "react";
import axios from "axios";
import NaverMap from "../components/NaverMap";

function ApproverAssignment() {
  const [addresses, setAddresses] = useState([]);

  const [emdList, setEmdList] = useState([]);
  const [selectedEmd, setSelectedEmd] = useState("");

  const [selectedLocation, setSelectedLocation] = useState({
    latitude: 35.228,
    longitude: 128.889,
  });

  const [errorMessage, setErrorMessage] = useState("");

  // 조사자 상태
  const [users, setUsers] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);
  const [userKeyword, setUserKeyword] = useState("");

  // 선택된 건물들
  const [selectedBuildings, setSelectedBuildings] = useState([]);

  // ---- 배정 여부 판단 & 필터 -------------------------------------------------
  const isAssigned = (b) =>
      b?.assigned === true ||
      b?.assigned === 1 ||
      b?.assigned === "true" ||
      b?.status === 1 ||
      b?.status === "ASSIGNED" ||
      b?.approverId != null ||
      b?.approver?.id != null ||
      b?.user?.id != null;

  const assignedAddresses = useMemo(
      () => (Array.isArray(addresses) ? addresses.filter(isAssigned) : []),
      [addresses]
  );

  // ---- 배정된 조사자 표시 라벨(여러 응답 스키마 방어) -------------------------
  // ✅ 연구원(조사원)만 표시. approver 관련 키는 전부 무시!
  const getAssignedUserLabel = (addr) => {
    // 1) 이름 후보 (researcher 쪽 우선)
    const name =
        addr?.researcherName ??
        addr?.assignedUserName ??
        addr?.userName ??
        addr?.user?.name ??
        addr?.researcher?.name ??
        null;

    // 2) 아이디(계정) 후보
    const username =
        addr?.researcherUsername ??
        addr?.assignedUsername ??
        addr?.username ??
        addr?.user?.username ??
        addr?.researcher?.username ??
        null;

    // 3) 숫자 ID 후보
    const id =
        addr?.researcherUserId ??
        addr?.userId ??
        addr?.user?.id ??
        addr?.assignedUserId ??
        null;

    // 4) 표기 규칙
    if (name && username) return `${name} (${username})`;
    if (name) return name;
    if (username) return `(${username})`;
    if (id != null) return `#${id}`;

    // 이 리스트는 '조사원 배정 O'가 전제라서 이 지점까지 거의 오지 않음.
    // 그래도 방어적으로 최소한 ID 성격을 표시.
    return "(조사원 정보 확인 필요)";
  };


  // =============================
  // 초기 로딩
  // =============================
  useEffect(() => {
    axios
        .get("/web/building/eupmyeondong")
        .then((res) => setEmdList(res.data))
        .catch((err) => console.error(err));

    handleSearch();

    axios
        .get("/web/api/approver/search")
        .then((res) => setUsers(res.data))
        .catch((err) => console.error("❌ 조사자 목록 불러오기 실패:", err));
  }, []);

  // =============================
  // 조사지 검색
  // =============================
  const handleSearch = () => {
    axios
        .get("/web/building/search/assigned", {
          params: { eupMyeonDong: selectedEmd || "" },
        })
        .then((res) => setAddresses(res.data))
        .catch((err) => console.error(err));
  };

  // =============================
  // 대상자 검색 (Enter로만 실행)
  // =============================
  const handleUserSearch = () => {
    axios
        .get("/web/building/pending-approval", { params: { keyword: userKeyword } })
        .then((res) => setUsers(res.data))
        .catch((err) => console.error("❌ 대상자 검색 실패:", err));
  };

  // =============================
  // 건물 체크박스 선택
  // =============================
  const handleBuildingCheck = (addr) => {
    const id = addr.id;
    const isChecked = selectedBuildings.includes(id);

    const updated = isChecked
        ? selectedBuildings.filter((bid) => bid !== id)
        : [...selectedBuildings, id];

    setSelectedBuildings(updated);

    if (!isChecked) {
      handleSelect(addr); // 마지막 선택 좌표로 이동
    }
  };

  // =============================
  // 지도 이동
  // =============================
  const handleSelect = (addr) => {
    const query = addr.lotAddress || addr.buildingName;
    if (!query) return;

    axios
        .get("/web/building/coords", { params: { address: query } })
        .then((res) => {
          if (res?.data?.latitude && res?.data?.longitude) {
            setSelectedLocation({
              latitude: res.data.latitude,
              longitude: res.data.longitude,
            });
            setErrorMessage("");
          } else {
            setErrorMessage(`좌표를 찾을 수 없습니다.\n요청한 주소: ${query}`);
          }
        })
        .catch((err) => {
          console.error("DB coords API error:", err);
          setErrorMessage("DB에서 좌표를 가져오는 중 오류가 발생했습니다.");
        });
  };

  // =============================
  // 배정 실행
  // =============================
  const handleAssign = async () => {
    if (!selectedUser) {
      alert("결재자를 선택하세요!");
      return;
    }
    if (selectedBuildings.length === 0) {
      alert("건물을 하나 이상 선택하세요!");
      return;
    }

    try {
      const res = await axios.post("/web/building/assign-approver", {
        userId: selectedUser.userId ?? selectedUser.id,
        buildingIds: selectedBuildings,
      });

      // 성공 후 갱신
      handleSearch();
      setSelectedBuildings([]);
      alert(`총 ${res?.data?.assignedCount ?? selectedBuildings.length}건이 배정되었습니다.`);
    } catch (err) {
      console.error("❌ 배정 실패:", err);
      alert("배정 중 오류가 발생했습니다.");
    }
  };

  // =============================
  // JSX
  // =============================
  return (
      <div className="container mt-4">
        <h2 className="mb-4">결재자 미배정 조사목록</h2>

        <div className="row">
          {/* 왼쪽: 결재자 미배정 조사지 목록 (현재는 assigned=true로 가져오므로 제목과 실제 필터가 다르면 제목을 조정하세요) */}
          <div className="col-md-8">
            <div className="p-3 border rounded bg-white shadow-sm">
              <div className="d-flex justify-content-between align-items-center mb-2">
                <h5 className="mb-0">결재자 미배정 조사지 목록</h5>
                <small className="text-muted">총 {assignedAddresses.length}건</small>
              </div>

              <ul className="list-group" style={{ maxHeight: "600px", overflowY: "auto" }}>
                {assignedAddresses.map((addr) => (
                    <li
                        key={addr.id}
                        className="list-group-item d-flex align-items-center"
                        style={{ cursor: "pointer" }}
                    >
                      <input
                          type="checkbox"
                          className="form-check-input me-2"
                          checked={selectedBuildings.includes(addr.id)}
                          onChange={() => handleBuildingCheck(addr)}
                      />

                      {/* 주소 */}
                      <span className="text-truncate">
                    {addr.lotAddress || addr.buildingName || `#${addr.id}`}
                  </span>

                      {/* 🔴 오른쪽 빨간 네모 박스: 배정된 조사자 표시 */}
                      <span
                          className="ms-auto px-2 py-1 border border-danger text-danger rounded"
                          style={{ minWidth: 150, textAlign: "center" }}
                          title="배정된 조사자"
                      >
                    {getAssignedUserLabel(addr)}
                  </span>
                    </li>
                ))}
              </ul>
            </div>
          </div>

          {/* 오른쪽: 지도 + 안내문 + 대상자 조회 */}
          <div className="col-md-4 d-flex flex-column gap-3">
            <NaverMap
                latitude={selectedLocation.latitude}
                longitude={selectedLocation.longitude}
            />

            {errorMessage && <div className="alert alert-warning mt-2">{errorMessage}</div>}

            <div className="p-3 border rounded bg-white shadow-sm">
              <h5 className="mb-3">결재자 조회</h5>

              <div className="input-group mb-3">
                <input
                    type="text"
                    className="form-control"
                    placeholder="이름 또는 아이디 입력"
                    value={userKeyword}
                    onChange={(e) => setUserKeyword(e.target.value)}
                    onKeyDown={(e) => e.key === "Enter" && handleUserSearch()}
                />
                <button
                    className="btn btn-primary"
                    style={{ backgroundColor: "#289eff", border: "none" }}
                    onClick={handleUserSearch}
                >
                  검색
                </button>
              </div>

              <ul className="list-group mb-3" style={{ maxHeight: "200px", overflowY: "auto" }}>
                {users.map((user) => (
                    <li key={user.userId ?? user.id} className="list-group-item d-flex align-items-center">
                      <input
                          type="radio"
                          name="userSelect"
                          className="form-check-input me-2"
                          onChange={() => setSelectedUser(user)}
                      />
                      {user.name} {user.username ? `(${user.username})` : ""}
                    </li>
                ))}
              </ul>

              <button
                  className="btn btn-outline-primary w-100"
                  style={{ borderColor: "#289eff", color: "#289eff" }}
                  disabled={!selectedUser || selectedBuildings.length === 0}
                  onClick={handleAssign}
              >
                배정
              </button>
            </div>
          </div>
        </div>
      </div>
  );
}

export default ApproverAssignment;
