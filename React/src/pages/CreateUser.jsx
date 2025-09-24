import { useState, useEffect } from "react";
import axios from "axios";

function CreateUser() {
    const [formData, setFormData] = useState({
        name: "",
        username: "",
        password: "",
        empNo: "",
        preferredRegion: "", // ✅ 선호지역 추가
    });

    const [regions, setRegions] = useState([]); // ✅ 선호지역 리스트
    const [usernameValid, setUsernameValid] = useState(null);
    const [loading, setLoading] = useState(false);

    // 📌 선호 지역 목록 불러오기
    useEffect(() => {
        axios
            .get("/web/api/users/preferred-regions?city=김해시")
            .then((res) => setRegions(res.data))
            .catch((err) => console.error("선호지역 불러오기 실패:", err));
    }, []);

    // 입력값 변경 핸들러
    const handleChange = async (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });

        if (name === "username") {
            if (value.trim() === "") {
                setUsernameValid(null);
                return;
            }

            try {
                setLoading(true);
                const res = await axios.get("/web/api/users/check-username", {
                    params: { username: value.trim() },
                });
                setUsernameValid(!res.data);
            } catch (err) {
                console.error("아이디 중복 확인 실패:", err);
                setUsernameValid(null);
            } finally {
                setLoading(false);
            }
        }
    };

    // 유효성 검사
    const isFormValid = () => {
        return (
            formData.name.trim() !== "" &&
            formData.username.trim() !== "" &&
            formData.password.trim() !== "" &&
            formData.empNo.trim() !== "" &&
            formData.preferredRegion.trim() !== "" && // ✅ 필수값
            usernameValid === true
        );
    };

    // 사번 생성 버튼
    const handleGenerateEmpNo = async () => {
        try {
            const res = await axios.get("/web/api/users/generate-empno");
            setFormData({ ...formData, empNo: res.data });
        } catch (err) {
            alert("사번 생성 실패");
            console.error(err);
        }
    };

    // 등록 버튼
    const handleSubmit = async () => {
        try {
            await axios.post("/web/api/users", formData);
            alert("등록 성공");
            console.log("등록된 데이터:", formData);
        } catch (error) {
            alert("등록 실패");
            console.error("등록 오류:", error);
        }
    };

    // 스타일
    const cardStyle = {
        maxWidth: "500px",
        margin: "30px auto",
        padding: "20px",
        borderRadius: "12px",
        boxShadow: "0 4px 10px rgba(0,0,0,0.1)",
        background: "white",
    };

    const inputStyle = {
        width: "100%",
        padding: "8px",
        marginTop: "5px",
        marginBottom: "5px",
        borderRadius: "4px",
        border: "1px solid #ccc",
    };

    const buttonStyle = (enabled = true) => ({
        padding: "10px 20px",
        borderRadius: "6px",
        border: "none",
        marginRight: "10px",
        cursor: enabled ? "pointer" : "not-allowed",
        backgroundColor: enabled ? "#289eff" : "#ccc",
        color: "white",
        fontWeight: "bold",
    });

    return (
        <div style={cardStyle}>
            <h2 style={{ textAlign: "center", marginBottom: "20px" }}>조사원 신규 등록</h2>

            <label>이름</label>
            <input
                style={inputStyle}
                type="text"
                name="name"
                value={formData.name}
                onChange={handleChange}
            />

            <label>아이디</label>
            <input
                style={inputStyle}
                type="text"
                name="username"
                value={formData.username}
                onChange={handleChange}
            />
            {loading && <p style={{ color: "gray", fontSize: "13px" }}>아이디 확인 중...</p>}
            {usernameValid === false && (
                <p style={{ color: "red", fontSize: "13px" }}>사용 불가능한 아이디 입니다</p>
            )}
            {usernameValid === true && (
                <p style={{ color: "green", fontSize: "13px" }}>사용 가능한 아이디 입니다</p>
            )}

            <label>비밀번호</label>
            <input
                style={inputStyle}
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
            />

            {/* ✅ 선호 지역 선택 */}
            <label>선호 지역</label>
            <select
                style={inputStyle}
                name="preferredRegion"
                value={formData.preferredRegion}
                onChange={handleChange}
            >
                <option value="">-- 선택하세요 --</option>
                {regions.map((r, idx) => (
                    <option key={idx} value={r}>
                        {r}
                    </option>
                ))}
            </select>

            <label>사번</label>
            <div style={{ display: "flex", gap: "10px", alignItems: "center" }}>
                <input
                    style={{ ...inputStyle, flex: 1 }}
                    type="text"
                    name="empNo"
                    value={formData.empNo}
                    readOnly
                />
                <button
                    type="button"
                    style={{
                        height: "38px",
                        padding: "0 16px",
                        fontSize: "14px",
                        borderRadius: "6px",
                        cursor: "pointer",
                        backgroundColor: "#289eff",
                        color: "white",
                        border: "none",
                    }}
                    onClick={handleGenerateEmpNo}
                >
                    사번 생성
                </button>
            </div>

            <div style={{ textAlign: "right", marginTop: "20px" }}>
                <button
                    style={buttonStyle(isFormValid())}
                    onClick={handleSubmit}
                    disabled={!isFormValid()}
                >
                    등록
                </button>
            </div>
        </div>
    );
}

export default CreateUser;
