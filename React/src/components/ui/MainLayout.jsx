import "../../styles/theme.css";
import "../../styles/layout.css";
import Sidebar from "./Sidebar.jsx";
import {Outlet} from "react-router-dom";
import Logo from "../../assets/GimHappy.png";
import axios from "axios";

export default function MainLayout({ user, onLogout }){

    const roleText = (role) => {
        if (role === "ADMIN") return "관리자";
        if (role === "APPROVER") return "결재자";
        return role || "Guest";
    };

    const handleLogout = async () => {
        try{
            await axios.post("/web/api/auth/logout", {}, { withCredentials:true });
            onLogout?.();
        }catch(e){ console.error("로그아웃 실패:", e); }
    };

    return (
        <div className="app-shell">
            <Sidebar user={user} onLogout={onLogout}/>
            <main className="main">
                {/* 사용자 상태/로그아웃 (사이드바 최하단) */}
                <div className="user-footer justify-content-between" style={{ height: 50}}>
                    <div >
                        <div className="d-inline-block avatar" />
                        <span className="name ms-3">{user ? user.name : "로그인이 필요합니다"}</span>
                    </div>

                    <div>

                        <div style={{display:"flex" }} className="justify-content-end">
                        </div>
                        {user && (
                            <button className="logout" onClick={handleLogout} style={{ height: 30, textSize: 20}}>로그아웃</button>
                        )}
                    </div>
                </div>
                <section style={{ height: "87vh"}}>
                    <Outlet />
                </section>
            </main>
        </div>
    );
}
