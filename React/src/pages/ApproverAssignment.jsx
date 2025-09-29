// src/pages/ApproverAssignment.jsx
import { useEffect, useState } from "react";
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

    const [users, setUsers] = useState([]);
    const [selectedUser, setSelectedUser] = useState(null);
    const [userKeyword, setUserKeyword] = useState("");

    const [selectedBuildings, setSelectedBuildings] = useState([]);
    const selectedCount = selectedBuildings.length;

    useEffect(() => {
        axios
            .get("/web/building/eupmyeondong?city=김해시")
            .then((res) => setEmdList(res.data))
            .catch((err) => console.error(err));

        handleSearch();

        axios
            .get("/web/api/approver/search", { params: { keyword: "" } })
            .then((res) => setUsers(Array.isArray(res.data) ? res.data : []))
            .catch((err) => console.error("❌ 결재자 목록 로딩 실패:", err));
    }, []);

    const handleSearch = () => {
        axios
            .get("/web/building/pending-approval")
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

    const handleUserSearch = () => {
        axios
            .get("/web/api/approver/search", { params: { keyword: userKeyword } })
            .then((res) => setUsers(Array.isArray(res.data) ? res.data : []))
            .catch((err) => console.error("❌ 결재자 검색 실패:", err));
    };

    const handleBuildingCheck = (row) => {
        const id = row.id;
        const checked = selectedBuildings.includes(id);
        const next = checked
            ? selectedBuildings.filter((x) => x !== id)
            : [...selectedBuildings, id];
        setSelectedBuildings(next);

        if (!checked) handleLocate(row);
    };

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
            .catch(() => {
                setErrorMessage("DB에서 좌표를 가져오는 중 오류가 발생했습니다.");
            });
    };

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
            await handleSearch();
            setSelectedBuildings([]);
            alert(`총 ${data?.assignedCount ?? selectedBuildings.length}건이 배정되었습니다.`);
        } catch (err) {
            console.error("❌ 배정 실패:", err);
            alert("배정 중 오류가 발생했습니다.");
        }
    };

    const renderResearcherBadge = (addr) => {
        const name =
            addr?.assignedName ??
            addr?.researcherName ??
            addr?.user?.name ??
            null;
        return name ? name : "—";
    };

    return (
        <div className="container mt-4" style={{ fontFamily: "SCD, sans-serif" }}>
            {/* 타이틀 */}
            <div className="d-flex align-items-center mb-4">
                <i
                    className="bi bi-clipboard-check me-2"
                    style={{ color: "#6898FF", fontSize: "1.5rem" }}
                ></i>
                <h2 className="mb-0" style={{ color: "#6898FF", fontWeight: "bold" }}>
                    결재자 미배정 조사목록
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

                    <div className="col-md-4">
                        <button
                            className="btn w-100 fw-bold"
                            style={{ backgroundColor: "#6898FF", border: "none", color: "#fff" }}
                            onClick={handleSearchEMD}
                        >
                            조회
                        </button>
                    </div>
                </div>
            </div>

            <div className="row align-items-stretch">
                {/* 좌측: 결재자 미배정 목록 */}
                <div className="col-md-8 h-100 d-flex flex-column">
                    <div
                        className="p-3 border rounded bg-white shadow-sm d-flex flex-column"
                        style={{ height: "616px" }}
                    >
                        <div className="d-flex justify-content-between align-items-center mb-2">
                            <h5 className="mb-0" style={{ color: "#6898FF", fontWeight: "bold" }}>
                                결재자 미배정 조사지 목록
                            </h5>
                            <div className="d-flex align-items-center gap-2">
                                <span className="px-2 py-1 text-muted small">선택 {selectedCount}건</span>
                                <small className="text-muted">총 {addresses.length}건</small>
                            </div>
                        </div>

                        <ul className="list-group flex-grow-1" style={{ overflowY: "auto" }}>
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
                                        onClick={() => handleBuildingCheck(addr)}
                                    >
                                        <input
                                            type="checkbox"
                                            className="form-check-input me-2"
                                            checked={selectedBuildings.includes(addr.id)}
                                        />
                                        <span className="text-truncate">
                      {addr.lotAddress || addr.buildingName || `#${addr.id}`}
                    </span>

                                        <span
                                            className="ms-auto px-2 py-1 border rounded"
                                            style={{
                                                minWidth: 150,
                                                textAlign: "center",
                                                borderColor: "#6898FF",
                                                color: "#6898FF",
                                                fontWeight: "500",
                                            }}
                                        >
                      {renderResearcherBadge(addr)}
                    </span>
                                    </li>
                                ))
                            )}
                        </ul>
                    </div>
                </div>

                {/* 우측: 지도 + 결재자 조회 */}
                <div className="col-md-4 h-100 d-flex flex-column gap-3">
                    <div className="p-3 border rounded bg-white shadow-sm" style={{ height: "300px" }}>
                        <h5 className="mb-3" style={{ color: "#6898FF", fontWeight: "bold" }}>
                            지도
                        </h5>
                        <div style={{ height: "220px" }}>
                            <NaverMap latitude={selectedLocation.latitude} longitude={selectedLocation.longitude} />
                        </div>
                        {errorMessage && <div className="alert alert-warning mt-2">{errorMessage}</div>}
                    </div>

                    <div className="p-3 border rounded bg-white shadow-sm" style={{ height: "300px" }}>
                        <h5 className="mb-3" style={{ color: "#6898FF", fontWeight: "bold" }}>
                            결재자 조회
                        </h5>

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
                                className="btn"
                                style={{ backgroundColor: "#6898FF", border: "none", color: "#fff" }}
                                onClick={handleUserSearch}
                            >
                                검색
                            </button>
                        </div>

                        <ul className="list-group mb-3" style={{ maxHeight: "150px", overflowY: "auto" }}>
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
                            className="btn w-100 fw-bold"
                            style={{
                                backgroundColor:
                                    selectedUser && selectedBuildings.length > 0 ? "#6898FF" : "#ccc",
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
        </div>
    );
}

export default ApproverAssignment;
