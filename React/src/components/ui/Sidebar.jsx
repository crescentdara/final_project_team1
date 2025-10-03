import {NavLink} from "react-router-dom";
import Logo from "../../assets/GimHappy.png";
import "../../styles/layout.css";

const Item = ({to, icon, label}) => (
    <NavLink to={to} className={({isActive}) => "nav-item"+(isActive?" active":"")}>
        <span style={{width:18, textAlign:"center"}}>{icon}</span>
        <span>{label}</span>
    </NavLink>
);

export default function Sidebar(){


    return (
        <aside className="sidebar">
            {/* 상단 로고(투명 PNG 그대로) */}
            <div className="brand">
                <img src={Logo} alt="GimHappy" />
            </div>

            {/* 카테고리 */}
            <div className="nav-group">
                <Item to="/" label="Dashboard"/>
                <Item to="/surveyIndex" label="전체 조사지 리스트"/>
                <Item to="/surveyList" label="미배정 조사지 목록"/>
                <Item to="/surveyRegister" label="조사지 등록" />
                <Item to="/createUser" label="조사원 생성"/>
                <Item to="/users" label="조사원 상세정보"/>
                <Item to="/approverAssignment" label="결재자 배정"/>
                <Item to="/approvals" label="결재 대기"/>
                <Item to="/resultReport" label="결재 완료"/>
                <div className="nav-sep" />
                <Item to="/login" label="로그인"/>
            </div>


        </aside>
    );
}
