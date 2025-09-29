import "../../styles/theme.css";
import "../../styles/layout.css";
import Sidebar from "./Sidebar.jsx";
import {Outlet} from "react-router-dom";

export default function MainLayout({ user, onLogout }){
    return (
        <div className="app-shell">
            <Sidebar user={user} onLogout={onLogout}/>
            <main className="main">
                <div className="top-bar">
                    <div className="banner">
                        <h3>업무 현황 보드</h3>
                        <button className="cta">빠른 작업</button>
                    </div>
                </div>
                <section>
                    <Outlet />
                </section>
            </main>
        </div>
    );
}
