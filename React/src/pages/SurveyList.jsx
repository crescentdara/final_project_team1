// import { useEffect, useState } from "react";
// import axios from "axios";
// import NaverMap from "../components/NaverMap"; // 지도 컴포넌트
//
// function SurveyList() {
//     const [addresses, setAddresses] = useState([]);
//     const [emdList, setEmdList] = useState([]);
//     const [selectedEmd, setSelectedEmd] = useState("");
//
//     const [selectedLocation, setSelectedLocation] = useState({
//         latitude: 35.228,
//         longitude: 128.889,
//     });
//     const [errorMessage, setErrorMessage] = useState("");
//
//     const [users, setUsers] = useState([]);
//     const [selectedUser, setSelectedUser] = useState(null);
//     const [userKeyword, setUserKeyword] = useState("");
//
//     const [selectedBuildings, setSelectedBuildings] = useState([]);
//     const selectedCount = selectedBuildings.length;
//
//     useEffect(() => {
//         axios
//             .get("/web/building/eupmyeondong?city=김해시")
//             .then((res) => setEmdList(res.data))
//             .catch((err) => console.error(err));
//
//         handleSearch();
//     }, []);
//
//     const handleSearch = () => {
//         axios
//             .get("/web/building/unassigned", {
//                 params: { region: selectedEmd || "" },
//             })
//             .then((res) => {
//                 setAddresses(res.data.results || []);
//                 setUsers(res.data.investigators || []);
//             })
//             .catch((err) => console.error("미배정 조사지 불러오기 실패:", err));
//     };
//
//     const handleUserSearch = () => {
//         axios
//             .get("/web/building/unassigned", {
//                 params: { region: selectedEmd || "", keyword: userKeyword || "" },
//             })
//             .then((res) => setUsers(res.data.investigators || []))
//             .catch((err) => console.error("조사원 검색 실패:", err));
//     };
//
//     const handleBuildingCheck = (addr) => {
//         const id = addr.id;
//         const isChecked = selectedBuildings.includes(id);
//         let updated;
//
//         if (isChecked) {
//             updated = selectedBuildings.filter((bid) => bid !== id);
//         } else {
//             updated = [...selectedBuildings, id];
//             handleSelect(addr);
//         }
//         setSelectedBuildings(updated);
//     };
//
//     const handleSelect = (addr) => {
//         let query = addr.lotAddress || addr.buildingName;
//         if (!query) return;
//
//         axios
//             .get("/web/building/coords", { params: { address: query } })
//             .then((res) => {
//                 if (res.data && res.data.latitude && res.data.longitude) {
//                     setSelectedLocation({
//                         latitude: res.data.latitude,
//                         longitude: res.data.longitude,
//                     });
//                     setErrorMessage("");
//                 } else {
//                     setErrorMessage(`좌표를 찾을 수 없습니다.\n요청한 주소: ${query}`);
//                 }
//             })
//             .catch(() => {
//                 setErrorMessage("DB에서 좌표를 가져오는 중 오류가 발생했습니다.");
//             });
//     };
//
//     const handleAssign = async () => {
//         if (!selectedUser) {
//             alert("조사자를 선택하세요!");
//             return;
//         }
//         if (selectedBuildings.length === 0) {
//             alert("건물을 하나 이상 선택하세요!");
//             return;
//         }
//
//         try {
//             const res = await axios.post("/web/building/assign", {
//                 userId: selectedUser.userId,
//                 buildingIds: selectedBuildings,
//             });
//             handleSearch();
//             setSelectedBuildings([]);
//             alert(`총 ${res.data.assignedCount}건이 배정되었습니다.`);
//         } catch (err) {
//             console.error("배정 실패:", err);
//             alert("배정 중 오류가 발생했습니다.");
//         }
//     };
//
//     return (
//         <div
//             className="container-fluid mt-4 p-4 shadow-sm rounded-3"
//             style={{ backgroundColor: "#fff" }}
//         >
//             {/* 타이틀 */}
//             <h3
//                 className="fw-bold mb-4"
//                 style={{ borderLeft: "4px solid #6898FF", paddingLeft: "12px" }}
//             >
//                 미배정 조사목록
//             </h3>
//
//             {/* 상단 레이아웃: 왼쪽 검색 스트립 / 오른쪽 조회 버튼 */}
//             <div className="row g-3 align-items-start mb-3">
//                 {/* 왼쪽: 검색 스트립 */}
//                 <div className="col-md-8">
//                     <div className="border rounded p-3 bg-light shadow-sm">
//                         <div className="row g-3">
//                             {/*<div className="col-md-6">*/}
//                             {/*    <label className="form-label fw-bold">시/도 구분</label>*/}
//                             {/*    <select className="form-select" disabled>*/}
//                             {/*        <option>경상남도 김해시</option>*/}
//                             {/*    </select>*/}
//                             {/*</div>*/}
//                             <div className="col-md-6">
//                                 <label className="form-label fw-bold">읍/면/동 구분</label>
//                                 <select
//                                     className="form-select"
//                                     value={selectedEmd}
//                                     onChange={(e) => {
//                                         setSelectedEmd(e.target.value);
//                                         setTimeout(() => handleSearch(), 0);
//                                     }}
//                                 >
//                                     <option value="">전체</option>
//                                     {emdList.map((emd, idx) => (
//                                         <option key={idx} value={emd}>
//                                             {emd}
//                                         </option>
//                                     ))}
//                                 </select>
//                             </div>
//                         </div>
//                         {/* 오른쪽: 큰 조회 버튼(사이드 상단) */}
//                         <div className="col-md-4">
//                             <div className="border rounded p-3 bg-white shadow-sm">
//                                 <button
//                                     className="btn btn-lg w-100 fw-bold"
//                                     style={{ backgroundColor: "#289eff", border: "none", color: "#fff" }}
//                                     onClick={handleSearch}
//                                 >
//                                     조회
//                                 </button>
//                             </div>
//                         </div>
//                     </div>
//                 </div>
//
//             </div>
//
//             {/* 본문: 좌측 리스트 / 우측(지도 + 조사원 배정) */}
//             <div className="row g-3">
//                 {/* 좌측: 미배정 조사지 목록 */}
//                 <div className="col-md-8 d-flex flex-column">
//                     <div
//                         className="p-3 border rounded bg-white shadow-sm d-flex flex-column"
//                         style={{ height: "616px" }}
//                     >
//                         <div className="d-flex justify-content-between align-items-center mb-2">
//                             <h5 className="mb-0">미배정 조사지 목록</h5>
//                             <div className="d-flex align-items-center gap-2">
//                                 <span className="px-2 py-1 text-muted small">선택 {selectedCount}건</span>
//                                 <small className="text-muted">총 {addresses.length}건</small>
//                             </div>
//                         </div>
//
//                         <ul className="list-group flex-grow-1" style={{ overflowY: "auto" }}>
//                             {addresses.map((addr) => (
//                                 <li
//                                     key={addr.id}
//                                     className="list-group-item d-flex align-items-center"
//                                     style={{ cursor: "pointer" }}
//                                 >
//                                     <input
//                                         type="checkbox"
//                                         className="form-check-input me-2"
//                                         checked={selectedBuildings.includes(addr.id)}
//                                         onChange={() => handleBuildingCheck(addr)}
//                                     />
//                                     {addr.lotAddress || addr.buildingName}
//                                 </li>
//                             ))}
//                         </ul>
//                     </div>
//                 </div>
//
//                 {/* 우측: 조회(상단에서 이미 배치) + 지도 + 조사원 조회/배정 */}
//                 <div
//                     className="col-md-4 d-flex flex-column gap-3 position-sticky"
//                     style={{ top: "12px" }}
//                 >
//                     {/* 지도 */}
//                     <div className="p-3 border rounded bg-white shadow-sm" style={{ height: "300px" }}>
//                         <h5 className="mb-3">지도</h5>
//                         <div style={{ height: "220px" }}>
//                             <NaverMap latitude={selectedLocation.latitude} longitude={selectedLocation.longitude} />
//                         </div>
//                         {errorMessage && <div className="alert alert-warning mt-2">{errorMessage}</div>}
//                     </div>
//
//                     {/* 조사원 조회/배정 */}
//                     <div
//                         className="p-3 border rounded bg-white shadow-sm d-flex flex-column"
//                         style={{ height: "300px" }}
//                     >
//                         <h5 className="mb-3">조사원 조회</h5>
//                         <div className="input-group mb-3">
//                             <input
//                                 type="text"
//                                 className="form-control"
//                                 placeholder="이름 또는 아이디 입력"
//                                 value={userKeyword}
//                                 onChange={(e) => setUserKeyword(e.target.value)}
//                             />
//                             <button
//                                 className="btn"
//                                 style={{ backgroundColor: "#289eff", border: "none", color: "#fff" }}
//                                 onClick={handleUserSearch}
//                             >
//                                 검색
//                             </button>
//                         </div>
//
//                         <ul className="list-group mb-3 flex-grow-1" style={{ overflowY: "auto" }}>
//                             {users.map((user) => (
//                                 <li key={user.userId} className="list-group-item d-flex align-items-center">
//                                     <input
//                                         type="radio"
//                                         name="userSelect"
//                                         className="form-check-input me-2"
//                                         onChange={() => setSelectedUser(user)}
//                                     />
//                                     {user.name} ({user.username})
//                                 </li>
//                             ))}
//                         </ul>
//
//                         <button
//                             className="btn w-100 fw-bold mt-auto"
//                             style={{
//                                 backgroundColor: selectedUser && selectedBuildings.length > 0 ? "#289eff" : "#ccc",
//                                 color: "#fff",
//                                 border: "none",
//                             }}
//                             disabled={!selectedUser || selectedBuildings.length === 0}
//                             onClick={handleAssign}
//                         >
//                             배정
//                         </button>
//                     </div>
//                 </div>
//             </div>
//         </div>
//     );
// }
//
// export default SurveyList;

import {useEffect, useState} from "react";
import axios from "axios";
import NaverMap from "../components/NaverMap"; // 지도 컴포넌트

function SurveyList() {
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
    }, []);

    const handleSearch = () => {
        axios
            .get("/web/building/unassigned", {
                params: {region: selectedEmd || ""},
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
                params: {region: selectedEmd || "", keyword: userKeyword || ""},
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
            .get("/web/building/coords", {params: {address: query}})
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
        <div
            className="container-fluid p-4 shadow-sm rounded-3"
            style={{
                backgroundColor: "#fff",
                marginTop: 16,
                height: "calc(100vh - 110px)",   // 헤더/알림영역 높이에 맞춰 조정
                overflow: "hidden"               // 바깥쪽 스크롤 금지
            }}
        >
            {/* 타이틀 */}
            <h3
                className="fw-bold mb-3"
                style={{borderLeft: "4px solid #6898FF", paddingLeft: "12px"}}
            >
                미배정 조사지 목록
            </h3>

            {/* 두 컬럼 레이아웃 (랩 금지, 같은 높이) */}
            <div
                className="d-flex flex-nowrap gap-3 align-items-stretch"
                style={{height: "100%", }}
            >
                {/* ===================== 좌측 컬럼 ===================== */}
                <div className="d-flex flex-column"
                     style={{ flex: "0 0 65%", minWidth: 500, height: "100%" }}>
                    {/* 필터 카드 (드롭다운 + 조회 버튼) */}
                    <div className="border rounded p-2 bg-light shadow-sm mb-2 ">
                        <div className="d-flex align-items-center flex-nowrap">
                            {/* 라벨 + 셀렉트는 한 덩어리 */}
                            <div className="input-group input-group-sm" style={{minWidth: 300}}>
                                <span className="input-group-text fw-semibold">읍/면/동</span>
                                <select
                                    className="form-select form-select-sm"
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

                            {/* 세퍼레이터(데스크톱에서만 보이게) + 모바일 간격 보정 */}
                            {/*<div className="vr d-none d-md-block mx-2" style={{ height: 32 }} />*/}
                            {/* 모바일에서는 세퍼레이터 숨김이므로 좌측 마진으로 분리감 확보 */}
                            <button
                                className="btn btn-sm fw-bold ms-2"
                                style={{
                                    backgroundColor: "#289eff",
                                    border: "none",
                                    color: "#fff",
                                    minWidth: 80,
                                }}
                                onClick={handleSearch}
                            >
                                조회
                            </button>
                        </div>
                    </div>


                    {/* 미배정 조사지 목록 */}
                    <div
                        className="p-3 border rounded bg-white shadow-sm d-flex flex-column mb-5"
                        style={{ flex: 1, minHeight: 0 }}
                    >
                        <div className="d-flex justify-content-between align-items-center mb-2">
                            <h5 className="mb-0">미배정 조사지 목록</h5>
                            <div className="d-flex align-items-center gap-2">
                                <span className="px-2 py-1 text-muted small">선택 {selectedCount}건</span>
                                <small className="text-muted">총 {addresses.length}건</small>
                            </div>
                        </div>

                        <ul className="list-group flex-grow-1" style={{ overflowY: "auto", minHeight: 0 }}>
                            {addresses.map((addr) => (
                                <li
                                    key={addr.id}
                                    className="list-group-item d-flex align-items-center"
                                    style={{cursor: "pointer"}}
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

                {/* ===================== 우측 컬럼(큰 지도) ===================== */}
                <div className="d-flex flex-column"
                     style={{ flex: "0 0 35%", minWidth: 400, height: "100%", paddingRight: "12px" }}>
                    <div
                        className="p-3 border rounded bg-white shadow-sm d-flex flex-column"
                        style={{ flex: 1, minHeight: 0, maxHeight: 300 }} // 큰 지도 영역
                    >
                        {/*<h4 className="mb-3">지도</h4>*/}
                        <div style={{height: "100%", borderRadius: "12px", overflow: "hidden"}}>
                            <NaverMap
                                latitude={selectedLocation.latitude}
                                longitude={selectedLocation.longitude}
                            />
                        </div>

                        {errorMessage && (
                            <div className="alert alert-warning mt-3">{errorMessage}</div>
                        )}
                    </div>
                    {/* 조사원 조회/배정 */}
                    <div
                        className="p-3 border rounded bg-white shadow-sm d-flex flex-column mt-3"
                        style={{ flex: "0 0 240px", overflow: "hidden" }}
                    >
                        {/* 헤더: 좌(제목) / 우(검색 인풋+버튼) 한 줄 */}
                        <div className="d-flex align-items-center justify-content-between gap-2 mb-3 flex-nowrap">
                            <h5 className="m-0">조사원 조회</h5>

                            {/* 검색 UI: 작게(input-group-sm), 우측에 고정폭 */}
                            <div className="input-group input-group-sm" style={{maxWidth: 230, flex: "0 0 auto"}}>
                                <input
                                    type="text"
                                    className="form-control"
                                    placeholder="이름 또는 아이디 입력"
                                    value={userKeyword}
                                    onChange={(e) => setUserKeyword(e.target.value)}
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

                        {/* 리스트: 남는 공간만 차지하고, 내부 스크롤 */}
                        <ul className="list-group mb-3 flex-grow-1" style={{ minHeight: 0, overflowY: "auto" }}>
                            {users.map((user) => (
                                <li
                                    key={user.userId}
                                    className="list-group-item d-flex align-items-center py-1" // 아이템 세로 패딩 축소
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

                        {/* 배정 버튼: 작게(btn-sm), 하단 고정 */}
                        <button
                            className="btn btn-sm w-100 fw-bold"
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
    );
}

export default SurveyList;
