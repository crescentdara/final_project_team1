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
                onLogin(res.data.info); // App에 user 상태 전달
                navigate("/"); // 로그인 성공 후 메인 페이지로 이동
            } else {
                setError(res.data.message);
            }
        } catch (err) {
            setError("로그인 요청 실패");
        }
    };

    return (
        <div className="d-flex justify-content-center align-items-center" style={{ height: "100vh" }}>
            <div className="card p-4 shadow" style={{ width: 360 }}>
                <h3 className="mb-3">로그인</h3>
                <form onSubmit={handleSubmit}>
                    <div className="mb-3">
                        <label>아이디</label>
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
                        <label>비밀번호</label>
                        <input
                            type="password"
                            className="form-control"
                            name="pw"
                            value={form.pw}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    {error && <p className="text-danger">{error}</p>}
                    <button type="submit" className="btn btn-primary w-100">로그인</button>
                </form>
            </div>
        </div>
    );
}
