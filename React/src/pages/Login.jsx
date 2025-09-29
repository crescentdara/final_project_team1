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
            style={{ height: "100vh"}}
        >
            <div
                className="p-4 shadow"
                style={{
                    width: 380,
                    borderRadius: "16px",
                    backgroundColor: "#fff",
                }}
            >
                <h3
                    className="mb-4 text-center"
                    style={{ color: "#6898FF", fontWeight: "bold" }}
                >
                    로그인
                </h3>
                <form onSubmit={handleSubmit}>
                    <div className="mb-3">
                        <label className="form-label fw-bold">아이디</label>
                        <input
                            type="text"
                            className="form-control"
                            name="id"
                            value={form.id}
                            onChange={handleChange}
                            required
                            style={{ borderRadius: "8px" }}
                        />
                    </div>
                    <div className="mb-3">
                        <label className="form-label fw-bold">비밀번호</label>
                        <input
                            type="password"
                            className="form-control"
                            name="pw"
                            value={form.pw}
                            onChange={handleChange}
                            required
                            style={{ borderRadius: "8px" }}
                        />
                    </div>
                    {error && <p className="text-danger small">{error}</p>}
                    <button
                        type="submit"
                        className="btn w-100 fw-bold"
                        style={{
                            backgroundColor: "#6898FF",
                            border: "none",
                            color: "#fff",
                            borderRadius: "8px",
                            padding: "10px",
                            boxShadow: "0 4px 12px rgba(104,152,255,0.3)",
                        }}
                    >
                        로그인
                    </button>
                </form>
            </div>
        </div>
    );
}
