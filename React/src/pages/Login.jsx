import { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

export default function Login({ onLogin }) {
    const [form, setForm] = useState({ id: "", pw: "" });
    const [error, setError] = useState("");
    const navigate = useNavigate();

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const res = await axios.post("/web/api/auth/login", form, { withCredentials: true });
            if (res.data.success) {
                console.log("✅ 로그인 성공, 받은 info:", res.data.info);
                onLogin(res.data.info);
                navigate("/");
            } else {
                setError(res.data.message);
            }
        } catch (err) {
            setError("로그인 요청 실패");
        }
    };

    return (
        <div
            className="d-flex justify-content-center align-items-center"
            style={{ minHeight: "100vh" }}
        >
            <div
                className="p-4 shadow-sm rounded-3 bg-white"
                style={{ width: 380 }}
            >
                {/* 타이틀 */}
                <h3
                    className="fw-bold mb-4"
                    style={{
                        borderLeft: "4px solid #6898FF",
                        paddingLeft: "12px",
                    }}
                >
                    로그인
                </h3>

                {/* 폼 */}
                <form onSubmit={handleSubmit}>
                    <div className="mb-3">
                        <label className="form-label">아이디</label>
                        <input
                            type="text"
                            className="form-control"
                            name="id"
                            value={form.id}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div className="mb-3">
                        <label className="form-label">비밀번호</label>
                        <input
                            type="password"
                            className="form-control"
                            name="pw"
                            value={form.pw}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    {error && <p className="text-danger small">{error}</p>}

                    <button
                        type="submit"
                        className="btn w-100"
                        style={{ backgroundColor: "#6898FF", color: "#fff" }}
                    >
                        로그인
                    </button>
                </form>
            </div>
        </div>
    );
}
