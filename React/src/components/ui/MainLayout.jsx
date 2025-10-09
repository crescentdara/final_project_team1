import "../../styles/theme.css";
import "../../styles/layout.css";
import Sidebar from "./Sidebar.jsx";
import axios from "axios";
import {Outlet} from "react-router-dom";

export default function MainLayout({ user, onLogout }){

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
                <div className="user-footer justify-content-between" style={{height:50}}>
                    <div className="d-flex flex-row align-items-center">
                        <div className="d-inline-block avatar" />
                        <span className="name ms-2">{user ? user.name : "로그인이 필요합니다"}</span>
                    </div>
                    {user && (
                        <button className="logout" onClick={handleLogout} style={{height:30}}>로그아웃</button>
                    )}
                </div>
                <section style={{height:"88vh"}}>
                    <Outlet />
                </section>
            </main>
        </div>
    );
}
