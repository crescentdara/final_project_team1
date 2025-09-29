import { useEffect, useState } from "react";
import axios from "axios";
import NaverMap from "../components/NaverMap"; // 지도 컴포넌트

function SurveyList() {
    // 👉 useState 훅들 먼저 선언
    const [addresses, setAddresses] = useState([]);
    const [emdList, setEmdList] = useState([]);
    const [selectedEmd, setSelectedEmd] = useState("");
    const [selectedLocation, setSelectedLocation] = useState({
        latitude: 35.228,
        longitude: 128.889,
    });
    const [errorMessage, setErrorMessage] = useState("");
    const [users, setUsers] = useState([]);
    const [selectedUser, setSelectedUser] = useState(null);
    const [userKeyword, setUserKeyword] = useState("");
    const [selectedBuildings, setSelectedBuildings] = useState([]);

    // 👉 상태 변수 이후에 count 계산
    const selectedCount = selectedBuildings.length;

    useEffect(() => {
        axios
            .get("/web/building/eupmyeondong?city=김해시")
            .then((res) => setEmdList(res.data))
            .catch((err) => console.error(err));

        handleSearch();
    }, []);

    const handleSearch = () => {
        axios
            .get("/web/building/unassigned", {
                params: { region: selectedEmd || "" },
            })
            .then((res) => {
                setAddresses(res.data.results || []);
                setUsers(res.data.investigators || []);
            })
            .catch((err) => console.error("미배정 조사지 불러오기 실패:", err));
    };

    const handleUserSearch = () => {
        axios
            .get("/web/building/unassigned", {
                params: { region: selectedEmd || "", keyword: userKeyword || "" },
            })
            .then((res) => setUsers(res.data.investigators || []))
            .catch((err) => console.error("조사원 검색 실패:", err));
    };

    const handleBuildingCheck = (addr) => {
        const id = addr.id;
        const isChecked = selectedBuildings.includes(id);
        let updated;

        if (isChecked) {
            updated = selectedBuildings.filter((bid) => bid !== id);
        } else {
            updated = [...selectedBuildings, id];
            handleSelect(addr);
        }
        setSelectedBuildings(updated);
    };

    const handleSelect = (addr) => {
        let query = addr.lotAddress || addr.buildingName;
        if (!query) return;

        axios
            .get("/web/building/coords", { params: { address: query } })
            .then((res) => {
                if (res.data?.latitude && res.data?.longitude) {
                    setSelectedLocation({
                        latitude: res.data.latitude,
                        longitude: res.data.longitude,
                    });
                    setErrorMessage("");
                } else {
                    setErrorMessage(`좌표를 찾을 수 없습니다.\n요청한 주소: ${query}`);
                }
            })
            .catch(() => {
                setErrorMessage("DB에서 좌표를 가져오는 중 오류가 발생했습니다.");
            });
    };

    const handleAssign = async () => {
        if (!selectedUser) {
            alert("조사자를 선택하세요!");
            return;
        }
        if (selectedBuildings.length === 0) {
            alert("건물을 하나 이상 선택하세요!");
            return;
        }

        try {
            const res = await axios.post("/web/building/assign", {
                userId: selectedUser.userId,
                buildingIds: selectedBuildings,
            });

            handleSearch();
            setSelectedBuildings([]);
            alert(`총 ${res.data.assignedCount}건이 배정되었습니다.`);
        } catch (err) {
            console.error("배정 실패:", err);
            alert("배정 중 오류가 발생했습니다.");
        }
    };

    return (
        <div className="container mt-4" style={{ fontFamily: "SCD, sans-serif" }}>
            {/* 타이틀 */}
            <div className="d-flex align-items-center mb-4">
                <i
                    className="bi bi-list-check me-2"
                    style={{ color: "#6898FF", fontSize: "1.5rem" }}
                ></i>
                <h2 className="mb-0" style={{ color: "#6898FF", fontWeight: "bold" }}>
                    미배정 조사목록
                </h2>
            </div>

            {/* 검색 박스 */}
            <div className="border rounded p-3 mb-4 bg-light shadow-sm">
                <div className="row g-3 align-items-end">
                    <div className="col-md-4">
                        <label className="form-label fw-bold">시/도 구분</label>
                        <select className="form-select" disabled>
                            <option>경상남도 김해시</option>
                        </select>
                    </div>

                    <div className="col-md-4">
                        <label className="form-label fw-bold">읍/면/동 구분</label>
                        <select
                            className="form-select"
                            value={selectedEmd}
                            onChange={(e) => {
                                setSelectedEmd(e.target.value);
                                setTimeout(() => handleSearch(), 0);
                            }}
                        >
                            <option value="">전체</option>
                            {emdList.map((emd, idx) => (
                                <option key={idx} value={emd}>
                                    {emd}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="col-md-4">
                        <button
                            className="btn w-100 fw-bold"
                            style={{ backgroundColor: "#6898FF", border: "none", color: "#fff" }}
                            onClick={handleSearch}
                        >
                            조회
                        </button>
                    </div>
                </div>
            </div>

            <div className="row align-items-stretch">
                {/* 왼쪽: 조사지 목록 */}
                <div className="col-md-8 h-100 d-flex flex-column">
                    <div
                        className="p-3 border rounded bg-white shadow-sm d-flex flex-column"
                        style={{ height: "616px" }} // 고정 높이
                    >
                        <div className="d-flex justify-content-between align-items-center mb-2">
                            <h5 className="mb-0" style={{ color: "#6898FF", fontWeight: "bold" }}>
                                미배정 조사지 목록
                            </h5>
                            <div className="d-flex align-items-center" style={{ gap: "12px" }}>
                                <span className="text-muted small">선택 {selectedCount}건</span>
                                <small className="text-muted">총 {addresses.length}건</small>
                            </div>
                        </div>

                        {/* 리스트만 스크롤 */}
                        <ul
                            className="list-group flex-grow-1"
                            style={{ overflowY: "auto" }}
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

                {/* 오른쪽: 지도 + 조사원 */}
                <div className="col-md-4 h-100 d-flex flex-column gap-3">
                    {/* 지도 카드 */}
                    <div
                        className="p-3 border rounded bg-white shadow-sm flex-grow-1"
                        style={{ height: "300px" }} // 높이 조정
                    >
                        <h5 className="mb-3" style={{ color: "#6898FF", fontWeight: "bold" }}>
                            지도
                        </h5>
                        <div style={{ height: "220px" }}>
                            <NaverMap
                                latitude={selectedLocation.latitude}
                                longitude={selectedLocation.longitude}
                            />
                        </div>
                    </div>

                    {/* 조사원 조회 */}
                    <div
                        className="p-3 border rounded bg-white shadow-sm"
                        style={{ height: "300px" }} // 높이 조정
                    >
                        <h5 className="mb-3" style={{ color: "#6898FF", fontWeight: "bold" }}>
                            조사원 조회
                        </h5>

                        <div className="input-group mb-3">
                            <input
                                type="text"
                                className="form-control"
                                placeholder="이름 또는 아이디 입력"
                                value={userKeyword}
                                onChange={(e) => setUserKeyword(e.target.value)}
                            />
                            <button
                                className="btn"
                                style={{
                                    backgroundColor: "#6898FF",
                                    border: "none",
                                    color: "#fff",
                                }}
                                onClick={handleUserSearch}
                            >
                                검색
                            </button>
                        </div>

                        <ul
                            className="list-group mb-3"
                            style={{ maxHeight: "150px", overflowY: "auto" }}
                        >
                            {users.map((user) => (
                                <li
                                    key={user.userId}
                                    className="list-group-item d-flex align-items-center"
                                    style={{ cursor: "pointer" }}
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
                            className="btn w-100 fw-bold"
                            style={{
                                backgroundColor:
                                    selectedUser && selectedBuildings.length > 0
                                        ? "#6898FF"
                                        : "#ccc",
                                border: "none",
                                color: "#fff",
                                borderRadius: "8px",
                                boxShadow:
                                    selectedUser && selectedBuildings.length > 0
                                        ? "0 4px 10px rgba(104,152,255,0.3)"
                                        : "none",
                            }}
                            disabled={!selectedUser || selectedBuildings.length === 0}
                            onClick={handleAssign}
                        >
                            배정하기
                        </button>
                    </div>
                </div>
            </div>
            <br />
            <br />
            <br />
            <br />
        </div>
    );
}

export default SurveyList;
