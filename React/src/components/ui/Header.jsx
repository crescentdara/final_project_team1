import axios from "axios";

export default function Header({ user, onLogout }) {
    const handleLogout = async () => {
        try {
            await axios.post("/web/api/auth/logout", {}, { withCredentials: true });
            if (onLogout) onLogout(); // App에 상태 전달
        } catch (err) {
            console.error("로그아웃 실패:", err);
        }
    };

    const roleText = (role) => {
        if (role === "ADMIN") return "관리자";
        if (role === "APPROVER") return "결재자";
        return role;
    };

    return (
        <header className="d-flex justify-content-between align-items-center p-3 border-bottom bg-light">
            <h3>Office Log</h3>

            {user ? (
                <div>
                    <span className="me-2">{user.name} ({roleText(user.role)})</span>
                    <button className="btn btn-sm btn-outline-secondary" onClick={handleLogout}>
                        로그아웃
                    </button>
                </div>
            ) : (
                <span>로그인 해주세요</span>
            )}
        </header>
    );
}
