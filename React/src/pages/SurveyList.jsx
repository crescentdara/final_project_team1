import { useEffect, useState } from "react";
import axios from "axios";
import NaverMap from "../components/NaverMap"; // 새로 만든 지도 컴포넌트 import

function SurveyList() {
    const [addresses, setAddresses] = useState([]);
    const [emdList, setEmdList] = useState([]);   // 읍면동 목록
    const [selectedEmd, setSelectedEmd] = useState(""); // 선택된 읍면동
    const [selectedLocation, setSelectedLocation] = useState({
        latitude: 35.228,   // 기본 좌표 (김해 중심 정도)
        longitude: 128.889,
    });
    const [errorMessage, setErrorMessage] = useState(""); // 안내문 메시지

    useEffect(() => {
        // 읍면동 옵션 불러오기
        axios.get("/building/eupmyeondong?city=김해시")
            .then(res => setEmdList(res.data))
            .catch(err => console.error(err));

        // 페이지 진입 시 전체 목록 불러오기
        handleSearch();  // 전체 호출
    }, []);

    // 검색 버튼 클릭 (재사용 가능)
    const handleSearch = () => {
        axios.get("/building/search", {
            params: { eupMyeonDong: selectedEmd || "" }
        })
            .then(res => setAddresses(res.data))
            .catch(err => console.error(err));
    };

    // 주소 전처리 함수
    const normalizeAddress = (query) => {
        if (!query) return "";

        let normalized = query.trim();

        // ✅ 0001-0031 → 1-31 형태로 변환
        normalized = normalized.replace(/(\d+)-0+(\d+)/, (_, 앞, 뒤) => `${parseInt(앞)}-${parseInt(뒤)}`);

        // ✅ 0094 → 94 형태 변환 (단독 숫자만)
        normalized = normalized.replace(/\b0+(\d+)/g, "$1");

        return normalized;
    };

    // 체크 시 → 주소 문자열을 네이버 지오코딩 API로 보냄
// ... 기존 import 유지

// 주소 선택 시 → DB에서 좌표 가져오기
    const handleSelect = (addr) => {
        let query = addr.lotAddress || addr.buildingName;
        if (!query) return;

        console.log("📍 DB 좌표 조회 요청:", query);

        axios.get("/building/coords", { params: { address: query } })
            .then(res => {
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
            .catch(err => {
                console.error("DB coords API error:", err);
                setErrorMessage("DB에서 좌표를 가져오는 중 오류가 발생했습니다.");
            });
    };

    return (
        <div className="container mt-4">
            <h2 className="mb-4">미배정 조사목록</h2>

            {/* 🔎 검색 박스 */}
            <div className="border rounded p-3 mb-4 bg-light shadow-sm">
                <div className="row g-3 align-items-end">
                    {/* 시/도 구분 */}
                    <div className="col-md-4">
                        <label className="form-label fw-bold">시/도 구분</label>
                        <select className="form-select" disabled>
                            <option>경상남도 김해시</option>
                        </select>
                    </div>

                    {/* 읍면동 구분 */}
                    <div className="col-md-4">
                        <label className="form-label fw-bold">읍/면/동 구분</label>
                        <select
                            className="form-select"
                            value={selectedEmd}
                            onChange={(e) => setSelectedEmd(e.target.value)}
                        >
                            <option value="">전체</option>
                            {emdList.map((emd, idx) => (
                                <option key={idx} value={emd}>{emd}</option>
                            ))}
                        </select>
                    </div>

                    {/* 조회 버튼 */}
                    <div className="col-md-4">
                        <button
                            className="btn btn-primary w-100 fw-bold"
                            style={{ backgroundColor: "#289eff", border: "none" }}
                            onClick={handleSearch}
                        >
                            조회
                        </button>
                    </div>
                </div>
            </div>

            <div className="row">
                {/* 왼쪽: 목록 */}
                <div className="col-md-8">
                    <div className="p-3 border rounded bg-white shadow-sm">
                        <div className="d-flex justify-content-between align-items-center mb-2">
                            <h5 className="mb-0">미배정 조사지 목록</h5>
                            <small className="text-muted">총 {addresses.length}건</small>
                        </div>
                        <ul
                            className="list-group"
                            style={{ maxHeight: "400px", overflowY: "auto" }}
                        >
                            {addresses.map((addr, index) => (
                                <li
                                    key={index}
                                    className="list-group-item d-flex align-items-center"
                                    style={{ cursor: "pointer" }}
                                >
                                    <input
                                        type="radio" // ✅ 단일 선택만 가능
                                        name="addressSelect"
                                        className="form-check-input me-2"
                                        onChange={() => handleSelect(addr)}
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
                        <div className="alert alert-warning mt-2">
                            {errorMessage}
                        </div>
                    )}

                    {/* 대상자 조회 */}
                    <div className="p-3 border rounded bg-white shadow-sm">
                        <h5 className="mb-3">대상자 조회</h5>
                        <button
                            className="btn btn-outline-primary w-100 mb-2"
                            style={{ borderColor: "#289eff", color: "#289eff" }}
                        >
                            배정
                        </button>

                        <button
                            className="btn btn-outline-primary w-100"
                            style={{ borderColor: "#289eff", color: "#289eff" }}
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
