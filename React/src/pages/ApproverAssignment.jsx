// src/pages/ApproverAssignment.jsx
import {useEffect, useState} from "react";
import axios from "axios";
import NaverMap from "../components/NaverMap";

function ApproverAssignment() {
    // 좌측 목록(조사원 배정 O + 결재자 미배정)
    const [addresses, setAddresses] = useState([]);

    // 읍/면/동
    const [emdList, setEmdList] = useState([]);
    const [selectedEmd, setSelectedEmd] = useState("");

    // 지도 상태
    const [selectedLocation, setSelectedLocation] = useState({
        latitude: 35.228,
        longitude: 128.889,
    });
    const [errorMessage, setErrorMessage] = useState("");

    // 결재자(Approver)
    const [users, setUsers] = useState([]);
    const [selectedUser, setSelectedUser] = useState(null);
    const [userKeyword, setUserKeyword] = useState("");

    // 체크된 건물들
    const [selectedBuildings, setSelectedBuildings] = useState([]);
    const selectedCount = selectedBuildings.length;

    // 초기 로딩
    useEffect(() => {
        axios
            .get("/web/building/eupmyeondong?city=김해시")
            .then((res) => setEmdList(res.data))
            .catch((err) => console.error(err));

        handleSearch(); // 결재자 미배정 목록
        axios
            .get("/web/api/approver/search", {params: {keyword: ""}})
            .then((res) => setUsers(Array.isArray(res.data) ? res.data : []))
            .catch((err) => console.error("결재자 목록 로딩 실패:", err));
    }, []);

    // 결재자 미배정 목록
    const handleSearch = () => {
        axios
            .get("/web/building/pending-approval")
            .then((res) => setAddresses(Array.isArray(res.data) ? res.data : []))
            .catch((err) => console.error("목록 로딩 실패:", err));
    };

    // 읍/면/동 필터 조회
    const handleSearchEMD = () => {
        axios
            .get("/web/building/pending-approval", {
                params: {eupMyeonDong: selectedEmd || ""},
            })
            .then((res) => setAddresses(Array.isArray(res.data) ? res.data : []))
            .catch((err) => console.error(err));
    };

    // 결재자 검색
    const handleUserSearch = () => {
        axios
            .get("/web/api/approver/search", {params: {keyword: userKeyword}})
            .then((res) => setUsers(Array.isArray(res.data) ? res.data : []))
            .catch((err) => console.error("결재자 검색 실패:", err));
    };

    // 체크 + 지도 이동
    const handleBuildingCheck = (row) => {
        const id = row.id;
        const checked = selectedBuildings.includes(id);
        const next = checked
            ? selectedBuildings.filter((x) => x !== id)
            : [...selectedBuildings, id];
        setSelectedBuildings(next);

        if (!checked) handleLocate(row);
    };

    // 주소 → 좌표
    const handleLocate = (row) => {
        const query = row.lotAddress || row.buildingName;
        if (!query) return;

        axios
            .get("/web/building/coords", {params: {address: query}})
            .then(({data}) => {
                if (data?.latitude && data?.longitude) {
                    setSelectedLocation({latitude: data.latitude, longitude: data.longitude});
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

    // 결재자 배정
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
            const {data} = await axios.post("/web/api/approver/assign", {
                userId: selectedUser.userId ?? selectedUser.id,
                buildingIds: selectedBuildings,
            });
            await handleSearch();
            setSelectedBuildings([]);
            alert(`총 ${data?.assignedCount ?? selectedBuildings.length}건이 배정되었습니다.`);
        } catch (err) {
            console.error("배정 실패:", err);
            alert("배정 중 오류가 발생했습니다.");
        }
    };

    // 파란 상자에 표기할 ‘조사원 이름’
    const renderResearcherBadge = (addr) => {
        const name =
            addr?.assignedName ??
            addr?.researcherName ??
            addr?.user?.name ??
            null;
        return name ? name : "—";
    };

    return (
        <>
            {/* 로컬 스타일: 배지/리스트 느낌 보정 */}
            <style>{`
        .pill-blue {
          min-width: 150px;
          text-align: center;
          border: 1px solid #289eff;
          color: #289eff;
          font-weight: 500;
          border-radius: .375rem;
        }
        .list-fixed {
          height: 520px;
        }
      `}</style>

            <div
                className="container-fluid p-4 shadow-sm rounded-3"
                style={{
                    backgroundColor: "#fff",
                    marginTop: 16,
                    height: "calc(100vh - 110px)",
                    overflow: "hidden"

                }}>
                {/* 타이틀 (미배정 페이지와 동일 톤) */}
                <h3 className="fw-bold mb-3" style={{borderLeft: "4px solid #6898FF", paddingLeft: "12px"}}>
                    결재자 배정
                </h3>

                {/* 두 컬럼 레이아웃: 좌(필터+리스트) / 우(큰 지도 + 결재자 조회) */}
                <div className="d-flex flex-nowrap align-items-stretch"
                     style={{ height: "100%", gap: 20 }}
                >
                    {/* ===================== 좌측 컬럼 ===================== */}
                    <div className="d-flex flex-column"
                         style={{ flex: "1 1 auto", minWidth: 500, height: "100%", minHeight: 0 }}
                    >
                        {/* 상단 슬림 필터 바 (읍/면/동 + 조회) */}
                        <div className="border rounded p-2 bg-light shadow-sm mb-2">
                            <div className="d-flex align-items-center flex-nowrap">
                                <div className="input-group input-group-sm" style={{minWidth: 300}}>
                                    <span className="input-group-text fw-semibold">읍/면/동</span>
                                    <select
                                        className="form-select form-select-sm"
                                        value={selectedEmd}
                                        onChange={(e) => {
                                            setSelectedEmd(e.target.value);
                                            // 선택 즉시 조회하려면 아래 한 줄 유지
                                            setTimeout(() => handleSearchEMD(), 0);
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

                                <button
                                    className="btn btn-sm fw-bold ms-2"
                                    style={{backgroundColor: "#289eff", border: "none", color: "#fff", minWidth: 80}}
                                    onClick={handleSearchEMD}
                                >
                                    조회
                                </button>
                            </div>
                        </div>

                        {/* 결재자 미배정 조사지 목록 */}
                        <div className="p-3 border rounded bg-white shadow-sm d-flex flex-column mb-5"
                             style={{ flex: 1, minHeight: 0 }}
                        >
                            <div className="d-flex justify-content-between align-items-center mb-2">
                                <h5 className="mb-0">결재자 미배정 조사지 목록</h5>
                                <div className="d-flex align-items-center gap-2">
                                    <span className="px-2 py-1 text-muted small">선택 {selectedCount}건</span>
                                    <small className="text-muted">총 {addresses.length}건</small>
                                </div>
                            </div>

                            <ul className="list-group flex-grow-1"
                                style={{ overflowY: "auto", minHeight: 0 }}>
                                {addresses.length === 0 ? (
                                    <li className="list-group-item text-center text-muted py-4">
                                        해당 목록이 없습니다.
                                    </li>
                                ) : (
                                    addresses.map((addr) => {
                                        const checked = selectedBuildings.includes(addr.id);
                                        return (
                                            <li
                                                key={addr.id}
                                                className="list-group-item d-flex align-items-center"
                                                style={{cursor: "pointer"}}
                                                onClick={() => handleBuildingCheck(addr)}
                                                title={addr.lotAddress || addr.buildingName || `#${addr.id}`}
                                            >
                                                <input
                                                    type="checkbox"
                                                    className="form-check-input me-2"
                                                    checked={checked}
                                                    onChange={(e) => {
                                                        e.stopPropagation();
                                                        handleBuildingCheck(addr);
                                                    }}
                                                />
                                                <span
                                                    className="text-truncate">{addr.lotAddress || addr.buildingName || `#${addr.id}`}</span>

                                                {/* 우측 파란 라벨: 이미 배정된 조사원 이름 */}
                                                <span className="ms-auto px-2 py-1 pill-blue">
                          {renderResearcherBadge(addr)}
                        </span>
                                            </li>
                                        );
                                    })
                                )}
                            </ul>
                        </div>
                    </div>

                    {/* ===================== 우측 컬럼 ===================== */}
                    <div className="d-flex flex-column"
                         style={{ flex: "0 0 400px", minWidth: 400, }}
                    >
                        {/* 큰 지도 (상단 고정 느낌) */}
                        <div
                           className="p-3 border rounded bg-white shadow-sm d-flex flex-column"
                           style={{ flex: 1, position: "sticky", top: 0, maxHeight: "330px" }}
                         >
                           <div style={{ borderRadius: 12, overflow: "hidden" }}>
                                <NaverMap latitude={selectedLocation.latitude} longitude={selectedLocation.longitude}/>
                            </div>
                            {errorMessage && <div className="alert alert-warning mt-2 mb-0">{errorMessage}</div>}
                        </div>

                        {/* 결재자 조회/배정 카드 (작게) */}
                        <div className="p-3 border rounded bg-white shadow-sm d-flex flex-column mt-3"
                             style={{ overflow: "hidden"}}>
                            <div className="d-flex align-items-center justify-content-between gap-2 mb-3 flex-nowrap">
                                <h5 className="m-0">결재자 조회</h5>
                                <div className="input-group input-group-sm" style={{maxWidth: 230, flex: "0 0 auto"}}>
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
                                        style={{backgroundColor: "#289eff", border: "none", color: "#fff"}}
                                        onClick={handleUserSearch}
                                    >
                                        검색
                                    </button>
                                </div>
                            </div>

                            <ul className="list-group mb-2 flex-grow-1 overflow-auto" style={{minHeight: 0}}>
                                {users.map((u) => (
                                    <li key={u.userId ?? u.id}
                                        className="list-group-item d-flex align-items-center py-1">
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
                                className="btn btn-sm w-100 fw-bold mt-auto"
                                style={{
                                    backgroundColor: selectedUser && selectedBuildings.length > 0 ? "#289eff" : "#ccc",
                                    color: "#fff",
                                    border: "none",
                                }}
                                disabled={!selectedUser || selectedBuildings.length === 0}
                                onClick={handleAssign}
                            >
                                배정
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}

export default ApproverAssignment;
