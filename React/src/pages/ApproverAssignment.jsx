import { useEffect, useState } from "react";
import axios from "axios";
import NaverMap from "../components/NaverMap"; // 지도 컴포넌트

function SurveyList() {
  const [addresses, setAddresses] = useState([]);

  const [emdList, setEmdList] = useState([]); // 읍면동 목록
  const [selectedEmd, setSelectedEmd] = useState(""); // 선택된 읍면동

  const [selectedLocation, setSelectedLocation] = useState({
    latitude: 35.228, // 기본 좌표 (김해 중심)
    longitude: 128.889,
  });

  const [errorMessage, setErrorMessage] = useState(""); // 안내문 메시지

  // 결재자 관련 상태
  const [users, setUsers] = useState([]); // 결재자 목록
  const [selectedUser, setSelectedUser] = useState(null); // 선택된 결재자
  const [userKeyword, setUserKeyword] = useState(""); // 🔍 대상자 검색어

  // ✅ 건물 선택 상태 (체크박스 다중 선택)
  const [selectedBuildings, setSelectedBuildings] = useState([]);

  // =============================
  // 초기 로딩: 읍면동 + 결재자 목록 + 전체 조사지
  // =============================
  useEffect(() => {
    // 읍면동 옵션 불러오기
    axios
        .get("/web/building/eupmyeondong?city=김해시")
        .then((res) => setEmdList(res.data))
        .catch((err) => console.error(err));

    // 전체 조사지 불러오기
    handleSearch();

    // 전체 결재자 불러오기
    axios
        .get("/web/api/approver/search")
        .then((res) => setUsers(res.data))
        .catch((err) => console.error("❌ 결재자 목록 불러오기 실패:", err));
  }, []);

  // =============================
  // 조사지 검색
  // =============================
  const handleSearch = () => {
    axios
        .get("/web/building/search", {
          params: { eupMyeonDong: selectedEmd || "" },
        })
        .then((res) => setAddresses(res.data))
        .catch((err) => console.error(err));
  };

  // =============================
  // 대상자 검색
  // =============================
  const handleUserSearch = () => {
    axios
        .get("/web/api/approver/search", { params: { keyword: userKeyword } })
        .then((res) => setUsers(res.data))
        .catch((err) => console.error("❌ 대상자 검색 실패:", err));
  };

  // =============================
  // 건물 체크박스 선택 핸들러
  // =============================
  const handleBuildingCheck = (addr) => {
    const id = addr.id;
    const isChecked = selectedBuildings.includes(id);

    let updated;
    if (isChecked) {
      updated = selectedBuildings.filter((bid) => bid !== id);
    } else {
      updated = [...selectedBuildings, id];
      // 마지막 선택된 건물 좌표로 지도 이동
      handleSelect(addr);
    }
    setSelectedBuildings(updated);
  };

  // =============================
  // 지도에 핑 찍기
  // =============================
  const handleSelect = (addr) => {
    let query = addr.lotAddress || addr.buildingName;
    if (!query) return;

    console.log("📍 DB 좌표 조회 요청:", query);

    axios
        .get("/web/building/coords", { params: { address: query } })
        .then((res) => {
          if (res.data && res.data.latitude && res.data.longitude) {
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
  // 배정 버튼 클릭
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
      const res = await axios.post("/web/building/assign", {
        UserId: selectedUser.UserId,
        buildingIds: selectedBuildings,
      });
      console.log("✅ 배정 완료:", res.data);

      // 성공 후 목록 갱신
      handleSearch();
      setSelectedBuildings([]);
      alert(`총 ${res.data.assignedCount}건이 배정되었습니다.`);
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
        <h2 className="mb-4">미배정 결재자 목록</h2>

        {/*/!* 🔎 검색 박스 *!/*/}
        {/*<div className="border rounded p-3 mb-4 bg-light shadow-sm">*/}
        {/*  <div className="row g-3 align-items-end">*/}
        {/*    /!* 시/도 *!/*/}
        {/*    <div className="col-md-4">*/}
        {/*      <label className="form-label fw-bold">시/도 구분</label>*/}
        {/*      <select className="form-select" disabled>*/}
        {/*        <option>경상남도 김해시</option>*/}
        {/*      </select>*/}
        {/*    </div>*/}

        {/*    /!* 읍면동 *!/*/}
        {/*    <div className="col-md-4">*/}
        {/*      <label className="form-label fw-bold">읍/면/동 구분</label>*/}
        {/*      <select*/}
        {/*          className="form-select"*/}
        {/*          value={selectedEmd}*/}
        {/*          onChange={(e) => setSelectedEmd(e.target.value)}*/}
        {/*      >*/}
        {/*        <option value="">전체</option>*/}
        {/*        {emdList.map((emd, idx) => (*/}
        {/*            <option key={idx} value={emd}>*/}
        {/*              {emd}*/}
        {/*            </option>*/}
        {/*        ))}*/}
        {/*      </select>*/}
        {/*    </div>*/}

        {/*    /!* 조회 버튼 *!/*/}
        {/*    <div className="col-md-4">*/}
        {/*      <button*/}
        {/*          className="btn btn-primary w-100 fw-bold"*/}
        {/*          style={{ backgroundColor: "#289eff", border: "none" }}*/}
        {/*          onClick={handleSearch}*/}
        {/*      >*/}
        {/*        조회*/}
        {/*      </button>*/}
        {/*    </div>*/}
        {/*  </div>*/}
        {/*</div>*/}

        <div className="row">
          {/* 왼쪽: 결재자 배정 대기 목록 */}
          <div className="col-md-8">
            <div className="p-3 border rounded bg-white shadow-sm">
              <div className="d-flex justify-content-between align-items-center mb-2">
                <h5 className="mb-0">결재자 배정 대기 목록</h5>
                <small className="text-muted">총 {addresses.length}건</small>
              </div>
              <ul
                  className="list-group"
                  style={{ maxHeight: "400px", overflowY: "auto" }}
              >
                {addresses.map((addr) => (
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
                      {addr.lotAddress || addr.buildingName}
                    </li>
                ))}
              </ul>
            </div>
          </div>

          {/* 오른쪽: 지도 + 안내문 + 대상자 조회 */}
          <div className="col-md-4 d-flex flex-column gap-3">
            {/* 지도 */}
            <NaverMap
                latitude={selectedLocation.latitude}
                longitude={selectedLocation.longitude}
            />

            {/* 안내문 */}
            {errorMessage && (
                <div className="alert alert-warning mt-2">{errorMessage}</div>
            )}

            {/* 대상자 조회 */}
            <div className="p-3 border rounded bg-white shadow-sm">
              <h5 className="mb-3">결재자 조회</h5>

              {/* 🔎 대상자 검색 */}
              <div className="input-group mb-3">
                <input
                    type="text"
                    className="form-control"
                    placeholder="이름 또는 아이디 입력"
                    value={userKeyword}
                    onChange={(e) => setUserKeyword(e.target.value)}
                />
                <button
                    className="btn btn-primary"
                    style={{ backgroundColor: "#289eff", border: "none" }}
                    onClick={handleUserSearch}
                >
                  검색
                </button>
              </div>

              <ul
                  className="list-group mb-3"
                  style={{ maxHeight: "200px", overflowY: "auto" }}
              >
                {users.map((user) => (
                    <li
                        key={user.userId}
                        className="list-group-item d-flex align-items-center"
                    >
                      <input
                          type="radio"
                          name="userSelect"
                          className="form-check-input me-2"
                          onChange={() => setSelectedUser(user)}
                      />
                      {user.name} ({user.username})
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

export default SurveyList;
