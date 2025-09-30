import { useState, useEffect } from "react";
import axios from "axios";

function CreateUser() {
    const [formData, setFormData] = useState({
        name: "",
        username: "",
        password: "",
        empNo: "",
        preferredRegion: "",
    });

    const [regions, setRegions] = useState([]);
    const [usernameValid, setUsernameValid] = useState(null);
    const [loading, setLoading] = useState(false);

    // 📌 선호 지역 불러오기
    useEffect(() => {
        axios
            .get("/web/api/users/preferred-regions?city=김해시")
            .then((res) => setRegions(res.data))
            .catch(() => console.error("선호지역 불러오기 실패"));
    }, []);

    const handleChange = async (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });

        if (name === "username") {
            if (!value.trim()) {
                setUsernameValid(null);
                return;
            }
            try {
                setLoading(true);
                const res = await axios.get("/web/api/users/check-username", {
                    params: { username: value.trim() },
                });
                setUsernameValid(!res.data);
            } catch {
                setUsernameValid(null);
            } finally {
                setLoading(false);
            }
        }
    };

    const isFormValid = () =>
        formData.name.trim() &&
        formData.username.trim() &&
        formData.password.trim() &&
        formData.empNo.trim() &&
        formData.preferredRegion.trim() &&
        usernameValid === true;

    const handleGenerateEmpNo = async () => {
        try {
            const res = await axios.get("/web/api/users/generate-empno");
            setFormData({ ...formData, empNo: res.data });
        } catch {
            alert("사번 생성 실패");
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await axios.post("/web/api/users", formData);
            alert("등록 성공");
        } catch {
            alert("등록 실패");
        }
    };

    // ---------- 스타일 ----------
    const wrapperStyle = {
        marginTop: 20,
        borderRadius: 16,
        boxShadow: "0 2px 6px rgba(0,0,0,0.08)",
        background: "#fff",
        overflow: "hidden",
    };

    const headerStyle = {
        padding: "16px 24px",
        fontWeight: 600,
        fontSize: 16,
        borderBottom: "1px solid #eee",
        background: "#f9fbff",
        color: "#289eff",
    };

    const formStyle = {
        padding: "32px 40px",
        maxWidth: 600,
    };

    const labelStyle = {
        fontSize: 14,
        fontWeight: 600,
        marginBottom: 6,
        color: "#333",
    };

    const inputStyle = {
        width: "100%",
        padding: "12px 14px",
        marginBottom: 16,
        borderRadius: 8,
        border: "1px solid #e5e7eb", // 기본 연한 그레이
        fontSize: 14,
        outline: "none",
        background: "#fafafa",
        transition: "all 0.2s",
    };

    const smallText = (color) => ({
        fontSize: 12,
        color,
        marginTop: -8,
        marginBottom: 12,
    });

    const btnBase = {
        padding: "10px 20px",
        borderRadius: 8,
        border: "none",
        fontWeight: 600,
        cursor: "pointer",
        transition: "all 0.2s",
    };

    const primaryBtn = (enabled) => ({
        ...btnBase,
        background: enabled ? "#289eff" : "#bcdcff",
        color: "#fff",
        cursor: enabled ? "pointer" : "not-allowed",
    });

    const secondaryBtn = {
        ...btnBase,
        background: "#f1f3f5",
        color: "#333",
    };

    return (
        <div style={wrapperStyle}>
            {/* 상단 헤더 */}
            <div style={headerStyle}>조사원 생성</div>

            {/* 폼 */}
            <form style={formStyle} onSubmit={handleSubmit}>
                <div>
                    <div style={labelStyle}>이름 *</div>
                    <input
                        style={inputStyle}
                        type="text"
                        name="name"
                        value={formData.name}
                        onChange={handleChange}
                        onFocus={(e) => (e.target.style.border = "1px solid #289eff")}
                        onBlur={(e) => (e.target.style.border = "1px solid #e5e7eb")}
                    />
                </div>

                <div>
                    <div style={labelStyle}>아이디 *</div>
                    <input
                        style={inputStyle}
                        type="text"
                        name="username"
                        value={formData.username}
                        onChange={handleChange}
                        onFocus={(e) => (e.target.style.border = "1px solid #289eff")}
                        onBlur={(e) => (e.target.style.border = "1px solid #e5e7eb")}
                    />
                    {loading && <p style={smallText("gray")}>아이디 확인 중...</p>}
                    {usernameValid === false && (
                        <p style={smallText("red")}>사용 불가능한 아이디입니다</p>
                    )}
                    {usernameValid === true && (
                        <p style={smallText("green")}>사용 가능한 아이디입니다</p>
                    )}
                </div>

                <div>
                    <div style={labelStyle}>비밀번호 *</div>
                    <input
                        style={inputStyle}
                        type="password"
                        name="password"
                        value={formData.password}
                        onChange={handleChange}
                        onFocus={(e) => (e.target.style.border = "1px solid #289eff")}
                        onBlur={(e) => (e.target.style.border = "1px solid #e5e7eb")}
                    />
                </div>

                <div>
                    <div style={labelStyle}>선호 지역 *</div>
                    <select
                        style={inputStyle}
                        name="preferredRegion"
                        value={formData.preferredRegion}
                        onChange={handleChange}
                        onFocus={(e) => (e.target.style.border = "1px solid #289eff")}
                        onBlur={(e) => (e.target.style.border = "1px solid #e5e7eb")}
                    >
                        <option value="">-- 선택하세요 --</option>
                        {regions.map((r, idx) => (
                            <option key={idx} value={r}>
                                {r}
                            </option>
                        ))}
                    </select>
                </div>

                <div>
                    <div style={labelStyle}>사번 *</div>
                    <div style={{ display: "flex", gap: "10px" }}>
                        <input
                            style={{ ...inputStyle, flex: 1, marginBottom: 0 }}
                            type="text"
                            name="empNo"
                            value={formData.empNo}
                            readOnly
                        />
                        <button
                            type="button"
                            style={secondaryBtn}
                            onClick={handleGenerateEmpNo}
                        >
                            사번 생성
                        </button>
                    </div>
                </div>

                {/* 버튼 */}
                <div style={{ textAlign: "start", marginTop: 24 }}>
                    <button
                        type="submit"
                        style={primaryBtn(isFormValid())}
                        disabled={!isFormValid()}
                    >
                        등록
                    </button>
                </div>
            </form>
        </div>
    );
}

export default CreateUser;
