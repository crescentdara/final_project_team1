import {NavLink} from "react-router-dom";
import Logo from "../../assets/GimHappy.png";
import axios from "axios";
import "../../styles/layout.css";

const Item = ({to, icon, label}) => (
    <NavLink to={to} className={({isActive}) => "nav-item"+(isActive?" active":"")}>
        <span style={{width:18, textAlign:"center"}}>{icon}</span>
        <span>{label}</span>
    </NavLink>
);

export default function Sidebar({ user, onLogout }){
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
        <aside className="sidebar">
            {/* 상단 로고(투명 PNG 그대로) */}
            <div className="brand">
                <img src={Logo} alt="GimHappy" />
            </div>

            {/* 카테고리 */}
            <div className="nav-group">
                <Item to="/" label="Dashboard"/>
                <Item to="/surveyList" label="미배정 조사지 목록"/>
                <Item to="/surveyIndex" label="전체 조사지 리스트"/>
                <Item to="/createSurvey" label="조사지 생성"/>
                <Item to="/createUser" label="조사원 생성"/>
                <Item to="/users" label="조사원 상세정보"/>
                <Item to="/approvals" label="결재 대기"/>
                <Item to="/resultReport" label="결재 완료"/>
                <Item to="/approverAssignment" label="결재자 배정"/>
                <Item to="/buildingUpload" label="다건 등록"/>
                <Item to="/messageTabs" label="메시지 전송"/>
                <div className="nav-sep" />
                <Item to="/login" label="로그인"/>
            </div>

            {/* 아래로 밀기 */}
            <div className="spacer" />

            {/* 사용자 상태/로그아웃 (사이드바 최하단) */}
            <div className="user-footer">
                <div className="avatar" />
                <div style={{display:"flex",flexDirection:"column"}}>
                    <span className="name">{user ? user.name : "로그인이 필요합니다"}</span>
                    <span className="role">{roleText(user?.role)}</span>
                </div>
                {user && (
                    <button className="logout" onClick={handleLogout}>로그아웃</button>
                )}
            </div>
        </aside>
    );
}
