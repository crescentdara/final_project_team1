// src/pages/ApproverAssignment.jsx
import { useEffect, useState } from "react";
import axios from "axios";
import NaverMap from "../components/NaverMap";

function ApproverAssignment() {
  // 좌측 목록(조사원 배정 O + 결재자 미배정)
  const [addresses, setAddresses] = useState([]);

  const [emdList, setEmdList] = useState([]); // 읍면동 목록
  const [selectedEmd, setSelectedEmd] = useState(""); // 선택된 읍면동

  // 지도 상태
  const [selectedLocation, setSelectedLocation] = useState({
    latitude: 35.228,
    longitude: 128.889,
  });
  const [errorMessage, setErrorMessage] = useState("");

  // 결재자(Approver) 검색/선택
  const [users, setUsers] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);
  const [userKeyword, setUserKeyword] = useState("");

  // 체크된 건물들
  const [selectedBuildings, setSelectedBuildings] = useState([]);

  // -------------------------
  // 초기 로딩
  // -------------------------
  useEffect(() => {
    axios
        .get("/web/building/eupmyeondong?city=김해시")
        .then((res) => setEmdList(res.data))
        .catch((err) => console.error(err));
    // 1) 결재자 미배정 목록 불러오기
    handleSearch();
    // 2) 기본 결재자 후보(approver) 불러오기
    axios
        .get("/web/api/approver/search", { params: { keyword: "" } })
        .then((res) => setUsers(Array.isArray(res.data) ? res.data : []))
        .catch((err) => console.error("❌ 결재자 목록 로딩 실패:", err));
  }, []);

  // -------------------------
  // 목록 조회 (서버가 이미 필터링된 데이터를 내려줌)
  // -------------------------
  const handleSearch = () => {
    axios
        .get("/web/building/pending-approval", { params: {} })
        .then((res) => setAddresses(Array.isArray(res.data) ? res.data : []))
        .catch((err) => console.error("❌ 목록 로딩 실패:", err));
  };

  const handleSearchEMD = () => {
    axios
        .get("/web/building/pending-approval", {
          params: { eupMyeonDong: selectedEmd || "" },
        })
        .then((res) => setAddresses(res.data))
        .catch((err) => console.error(err));
  };

  // -------------------------
  // 결재자(Approver) 검색
  // -------------------------
  const handleUserSearch = () => {
    axios
        .get("/web/api/approver/search", { params: { keyword: userKeyword } })
        .then((res) => setUsers(Array.isArray(res.data) ? res.data : []))
        .catch((err) => console.error("❌ 결재자 검색 실패:", err));
  };

  // -------------------------
  // 체크박스 토글 + 지도 이동
  // -------------------------
  const handleBuildingCheck = (row) => {
    const id = row.id;
    const checked = selectedBuildings.includes(id);
    const next = checked
        ? selectedBuildings.filter((x) => x !== id)
        : [...selectedBuildings, id];
    setSelectedBuildings(next);

    if (!checked) {
      handleLocate(row);
    }
  };

  // 주소 → 좌표 조회하여 지도 이동
  const handleLocate = (row) => {
    const query = row.lotAddress || row.buildingName;
    if (!query) return;

    axios
        .get("/web/building/coords", { params: { address: query } })
        .then(({ data }) => {
          if (data?.latitude && data?.longitude) {
            setSelectedLocation({ latitude: data.latitude, longitude: data.longitude });
            setErrorMessage("");
          } else {
            setErrorMessage(`좌표를 찾을 수 없습니다.\n요청한 주소: ${query}`);
          }
        })
        .catch((err) => {
          console.error("좌표 조회 실패:", err);
          setErrorMessage("DB에서 좌표를 가져오는 중 오류가 발생했습니다.");
        });
  };

  // -------------------------
  // 결재자 배정
  // -------------------------
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
      const { data } = await axios.post("/web/api/approver/assign", {
        userId: selectedUser.userId ?? selectedUser.id,
        buildingIds: selectedBuildings,
      });
      // 성공 후 목록 갱신 및 선택 초기화
      await handleSearch();
      setSelectedBuildings([]);
      alert(`총 ${data?.assignedCount ?? selectedBuildings.length}건이 배정되었습니다.`);
    } catch (err) {
      console.error("❌ 배정 실패:", err);
      alert("배정 중 오류가 발생했습니다.");
    }
  };

  // 파란 상자에 표시할 ‘조사원 이름’ (없으면 대시)
  const renderResearcherBadge = (addr) => {
    const name =
        addr?.assignedName ??
        addr?.researcherName ??
        addr?.user?.name ??
        null;
    return name ? name : "—";
  };

  return (
      <div className="container mt-4">
        <h2 className="mb-4">결재자 미배정 조사목록</h2>

        {/* 🔎 검색 박스 */}
        <div className="border rounded p-3 mb-4 bg-light shadow-sm">
          <div className="row g-3 align-items-end">
            {/* 시/도 */}
            <div className="col-md-4">
              <label className="form-label fw-bold">시/도 구분</label>
              <select className="form-select" disabled>
                <option>경상남도 김해시</option>
              </select>
            </div>

            {/* 읍면동 */}
            <div className="col-md-4">
              <label className="form-label fw-bold">읍/면/동 구분</label>
              <select
                  className="form-select"
                  value={selectedEmd}
                  onChange={(e) => setSelectedEmd(e.target.value)}
              >
                <option value="">전체</option>
                {emdList.map((emd, idx) => (
                    <option key={idx} value={emd}>
                      {emd}
                    </option>
                ))}
              </select>
            </div>

            {/* 조회 버튼 */}
            <div className="col-md-4">
              <button
                  className="btn btn-primary w-100 fw-bold"
                  style={{ backgroundColor: "#289eff", border: "none" }}
                  onClick={handleSearchEMD}
              >
                조회
              </button>
            </div>
          </div>
        </div>

        <div className="row">
          {/* 좌측: 결재자 미배정 목록 */}
          <div className="col-md-8">
            <div className="p-3 border rounded bg-white shadow-sm">
              <div className="d-flex justify-content-between align-items-center mb-2">
                <h5 className="mb-0">결재자 미배정 조사지 목록</h5>
                <small className="text-muted">총 {addresses.length}건</small>
              </div>

              <ul className="list-group" style={{ maxHeight: "450px", overflowY: "auto" }}>
                {addresses.length === 0 ? (
                    <li className="list-group-item text-center text-muted py-4">
                      해당 목록이 없습니다.
                    </li>
                ) : (
                    addresses.map((addr) => (
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

                          <span className="text-truncate">
                            {addr.lotAddress || addr.buildingName || `#${addr.id}`}
                          </span>

                          {/* 파란 배지: 조사원 이름 (없으면 —) */}
                          <span
                              className="ms-auto px-2 py-1 border border-primary-subtle rounded"
                              style={{ minWidth: 150, textAlign: "center" }}
                              title="배정된 조사원"
                          >
                            {renderResearcherBadge(addr)}
                          </span>
                        </li>
                    ))
                )}
              </ul>
            </div>
          </div>

          {/* 우측: 지도 + 결재자 조회/배정 */}
          <div className="col-md-4 d-flex flex-column gap-3">
            <NaverMap latitude={selectedLocation.latitude} longitude={selectedLocation.longitude} />

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
                {users.map((u) => (
                    <li key={u.userId ?? u.id} className="list-group-item d-flex align-items-center">
                      <input
                          type="radio"
                          name="approverSelect"
                          className="form-check-input me-2"
                          onChange={() => setSelectedUser(u)}
                      />
                      {u.name} {u.username ? `(${u.username})` : ""}
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
