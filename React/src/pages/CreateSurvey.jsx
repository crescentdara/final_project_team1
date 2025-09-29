// import { useEffect, useMemo, useState } from "react";
// import { useNavigate, useSearchParams } from "react-router-dom";
// import axios from "axios";
//
// function CreateSurvey() {
//     const navigate = useNavigate();
//     const [sp] = useSearchParams();
//     const editingId = sp.get("id");                     // ← ?id=123 이 있으면 편집모드
//     const editMode = useMemo(() => Boolean(editingId), [editingId]);
//     const [saving, setSaving] = useState(false);
//     const [loadingPrefill, setLoadingPrefill] = useState(false);
//
//     const [formData, setFormData] = useState({
//         lotAddress: "",
//         latitude: "",
//         longitude: "",
//         buildingName: "",
//         mainUseName: "",
//         structureName: "",
//         groundFloors: "",
//         basementFloors: "",
//         landArea: "",
//         buildingArea: "",
//     });
//
//     const [errors, setErrors] = useState({});
//
//     // ---------- helpers ----------
//     const hasValue = (v) =>
//         v !== "" && v !== null && v !== undefined && String(v).trim() !== "";
//
//     // 모든 10개 입력 필수 + 에러 없음
//     const requiredAll = [
//         "lotAddress",
//         "latitude",
//         "longitude",
//         "buildingName",
//         "mainUseName",
//         "structureName",
//         "groundFloors",
//         "basementFloors",
//         "landArea",
//         "buildingArea",
//     ];
//
//     const isFormValid = useMemo(() => {
//         const allFilled = requiredAll.every((k) => hasValue(formData[k]));
//         const noErrors = Object.values(errors).every((v) => !v);
//         return allFilled && noErrors;
//     }, [formData, errors]);
//
//     // 숫자 핸들러 (정수/소수점)
//     const handleNumberChange = (e, allowDecimal = false) => {
//         const { name, value } = e.target;
//         const regex = allowDecimal ? /^\d*\.?\d*$/ : /^\d*$/;
//
//         if (regex.test(value)) {
//             setFormData((prev) => ({ ...prev, [name]: value }));
//             setErrors((prev) => ({ ...prev, [name]: "" }));
//         } else {
//             setErrors((prev) => ({ ...prev, [name]: "숫자를 입력해주세요" }));
//         }
//     };
//
//     // 일반 텍스트 핸들러
//     const handleTextChange = (e) => {
//         const { name, value } = e.target;
//         setFormData((prev) => ({ ...prev, [name]: value }));
//         setErrors((prev) => ({ ...prev, [name]: "" }));
//     };
//
//     // 편집모드: 프리필
//     useEffect(() => {
//         if (!editMode) return;
//         (async () => {
//             try {
//                 setLoadingPrefill(true);
//                 const { data } = await axios.get(`/web/building/${editingId}`);
//                 setFormData({
//                     lotAddress: data?.lotAddress ?? "",
//                     latitude: data?.latitude != null ? String(data.latitude) : "",
//                     longitude: data?.longitude != null ? String(data.longitude) : "",
//                     buildingName: data?.buildingName ?? "",
//                     mainUseName: data?.mainUseName ?? "",
//                     structureName: data?.structureName ?? "",
//                     groundFloors: data?.groundFloors != null ? String(data.groundFloors) : "",
//                     basementFloors: data?.basementFloors != null ? String(data.basementFloors) : "",
//                     landArea: data?.landArea != null ? String(data.landArea) : "",
//                     buildingArea: data?.buildingArea != null ? String(data.buildingArea) : "",
//                 });
//             } catch (e) {
//                 console.error(e);
//                 alert("대상 조사지(건물)를 불러올 수 없습니다.");
//                 navigate(-1);
//             } finally {
//                 setLoadingPrefill(false);
//             }
//         })();
//     }, [editMode, editingId, navigate]);
//
//     // 전송 전 숫자 변환
//     const toIntOrNull = (s) => (s === "" || s == null ? null : parseInt(s, 10));
//     const toFloatOrNull = (s) => (s === "" || s == null ? null : parseFloat(s));
//
//     const buildPayload = () => ({
//         lotAddress: formData.lotAddress.trim(),
//         latitude: toFloatOrNull(formData.latitude),
//         longitude: toFloatOrNull(formData.longitude),
//         buildingName: formData.buildingName.trim(),
//         mainUseName: formData.mainUseName.trim(),
//         structureName: formData.structureName.trim(),
//         groundFloors: toIntOrNull(formData.groundFloors),
//         basementFloors: toIntOrNull(formData.basementFloors),
//         landArea: toFloatOrNull(formData.landArea),
//         buildingArea: toFloatOrNull(formData.buildingArea),
//     });
//
//     const handleSubmit = async (e) => {
//         e.preventDefault();
//
//         if (!isFormValid) {
//             alert("모든 필수 입력값을 올바르게 입력해 주세요.");
//             return;
//         }
//
//         try {
//             setSaving(true);
//             const payload = buildPayload();
//             if (editMode) {
//                 await axios.put(`/web/building/${editingId}`, payload);
//                 alert("수정되었습니다.");
//             } else {
//                 await axios.post("/web/building", payload);
//                 alert("저장 성공");
//             }
//             navigate("/", { replace: true });
//         } catch (error) {
//             console.error("저장 중 오류 발생:", error);
//             const msg = error?.response?.data?.message || error?.message || "저장 실패";
//             alert(msg);
//         } finally {
//             setSaving(false);
//         }
//     };
//
//     // ---------- styles ----------
//     const cardStyle = {
//         maxWidth: 780,
//         margin: "30px auto",
//         padding: "22px",
//         borderRadius: "12px",
//         boxShadow: "0 4px 12px rgba(0,0,0,0.08)",
//         background: "white",
//     };
//
//     const gridStyle = {
//         display: "grid",
//         gridTemplateColumns: "1fr 1fr",
//         gap: "14px 16px",
//     };
//
//     const gridStyleMobile = {
//         display: "grid",
//         gridTemplateColumns: "1fr",
//         gap: "12px",
//     };
//
//     const labelStyle = { fontSize: 14, fontWeight: 600, marginBottom: 4 };
//     const inputBase = {
//         width: "100%",
//         padding: "10px 12px",
//         borderRadius: 8,
//         border: "1px solid #ccd0d5",
//         outline: "none",
//     };
//     const errorText = { color: "#d93025", fontSize: 12, marginTop: 4 };
//     const footerStyle = { marginTop: 18, display: "flex", justifyContent: "flex-end", gap: 10 };
//     const btn = (enabled, variant = "primary") => ({
//         padding: "10px 16px",
//         borderRadius: 8,
//         border: "none",
//         cursor: enabled ? "pointer" : "not-allowed",
//         fontWeight: 700,
//         background:
//             variant === "secondary" ? (enabled ? "#6c757d" : "#a6acb1") : enabled ? "#289eff" : "#bcdcff",
//         color: "white",
//     });
//
//     // 반응형: 간단히 윈도우 폭 기준으로 컬럼수 전환
//     const twoCols = typeof window !== "undefined" ? window.innerWidth >= 720 : true;
//     const fieldWrap = twoCols ? gridStyle : gridStyleMobile;
//
//     return (
//         <form onSubmit={handleSubmit} style={cardStyle}>
//             <h2 style={{ textAlign: "center", marginBottom: 6 }}>
//                 {editMode ? "조사목록 수정" : "조사목록 생성"}
//             </h2>
//             <p style={{ textAlign: "center", color: "#666", marginBottom: 18 }}>
//                 {loadingPrefill ? "기존 데이터를 불러오는 중…" : "모든 항목은 필수입니다."}
//             </p>
//
//             <div style={{ marginBottom: 8, fontWeight: 700, fontSize: 15 }}>위치 정보</div>
//             <div style={fieldWrap}>
//                 {/* lotAddress */}
//                 <div>
//                     <div style={labelStyle}>번지주소 *</div>
//                     <input
//                         style={{ ...inputBase, borderColor: errors.lotAddress ? "#d93025" : inputBase.border }}
//                         type="text"
//                         name="lotAddress"
//                         placeholder="예) 경상남도 김해시 ..."
//                         value={formData.lotAddress}
//                         onChange={handleTextChange}
//                     />
//                     {errors.lotAddress && <div style={errorText}>{errors.lotAddress}</div>}
//                 </div>
//
//                 {/* latitude */}
//                 <div>
//                     <div style={labelStyle}>위도 *</div>
//                     <input
//                         style={{ ...inputBase, borderColor: errors.latitude ? "#d93025" : inputBase.border }}
//                         type="text"
//                         name="latitude"
//                         placeholder="예) 35.123456"
//                         value={formData.latitude}
//                         onChange={(e) => handleNumberChange(e, true)}
//                     />
//                     {errors.latitude && <div style={errorText}>{errors.latitude}</div>}
//                 </div>
//
//                 {/* longitude */}
//                 <div>
//                     <div style={labelStyle}>경도 *</div>
//                     <input
//                         style={{ ...inputBase, borderColor: errors.longitude ? "#d93025" : inputBase.border }}
//                         type="text"
//                         name="longitude"
//                         placeholder="예) 128.123456"
//                         value={formData.longitude}
//                         onChange={(e) => handleNumberChange(e, true)}
//                     />
//                     {errors.longitude && <div style={errorText}>{errors.longitude}</div>}
//                 </div>
//             </div>
//
//             <div style={{ marginTop: 18, marginBottom: 8, fontWeight: 700, fontSize: 15 }}>건물 정보</div>
//             <div style={fieldWrap}>
//                 {/* buildingName */}
//                 <div>
//                     <div style={labelStyle}>건물명 *</div>
//                     <input
//                         style={{ ...inputBase, borderColor: errors.buildingName ? "#d93025" : inputBase.border }}
//                         type="text"
//                         name="buildingName"
//                         placeholder="예) 인수타워"
//                         value={formData.buildingName}
//                         onChange={handleTextChange}
//                     />
//                     {errors.buildingName && <div style={errorText}>{errors.buildingName}</div>}
//                 </div>
//
//                 {/* mainUseName */}
//                 <div>
//                     <div style={labelStyle}>주용도 *</div>
//                     <input
//                         style={{ ...inputBase, borderColor: errors.mainUseName ? "#d93025" : inputBase.border }}
//                         type="text"
//                         name="mainUseName"
//                         placeholder="예) 업무시설"
//                         value={formData.mainUseName}
//                         onChange={handleTextChange}
//                     />
//                     {errors.mainUseName && <div style={errorText}>{errors.mainUseName}</div>}
//                 </div>
//
//                 {/* structureName */}
//                 <div>
//                     <div style={labelStyle}>구조명 *</div>
//                     <input
//                         style={{ ...inputBase, borderColor: errors.structureName ? "#d93025" : inputBase.border }}
//                         type="text"
//                         name="structureName"
//                         placeholder="예) 철근콘크리트구조"
//                         value={formData.structureName}
//                         onChange={handleTextChange}
//                     />
//                     {errors.structureName && <div style={errorText}>{errors.structureName}</div>}
//                 </div>
//
//                 {/* groundFloors */}
//                 <div>
//                     <div style={labelStyle}>지상층수 *</div>
//                     <input
//                         style={{ ...inputBase, borderColor: errors.groundFloors ? "#d93025" : inputBase.border }}
//                         type="text"
//                         name="groundFloors"
//                         placeholder="예) 10"
//                         value={formData.groundFloors}
//                         onChange={(e) => handleNumberChange(e, false)}
//                     />
//                     {errors.groundFloors && <div style={errorText}>{errors.groundFloors}</div>}
//                 </div>
//
//                 {/* basementFloors */}
//                 <div>
//                     <div style={labelStyle}>지하층수 *</div>
//                     <input
//                         style={{ ...inputBase, borderColor: errors.basementFloors ? "#d93025" : inputBase.border }}
//                         type="text"
//                         name="basementFloors"
//                         placeholder="예) 2"
//                         value={formData.basementFloors}
//                         onChange={(e) => handleNumberChange(e, false)}
//                     />
//                     {errors.basementFloors && <div style={errorText}>{errors.basementFloors}</div>}
//                 </div>
//             </div>
//
//             <div style={{ marginTop: 18, marginBottom: 8, fontWeight: 700, fontSize: 15 }}>면적 정보</div>
//             <div style={fieldWrap}>
//                 {/* landArea */}
//                 <div>
//                     <div style={labelStyle}>대지면적(㎡) *</div>
//                     <input
//                         style={{ ...inputBase, borderColor: errors.landArea ? "#d93025" : inputBase.border }}
//                         type="text"
//                         name="landArea"
//                         placeholder="예) 1234.56"
//                         value={formData.landArea}
//                         onChange={(e) => handleNumberChange(e, true)}
//                     />
//                     {errors.landArea && <div style={errorText}>{errors.landArea}</div>}
//                 </div>
//
//                 {/* buildingArea */}
//                 <div>
//                     <div style={labelStyle}>건축면적(㎡) *</div>
//                     <input
//                         style={{ ...inputBase, borderColor: errors.buildingArea ? "#d93025" : inputBase.border }}
//                         type="text"
//                         name="buildingArea"
//                         placeholder="예) 789.01"
//                         value={formData.buildingArea}
//                         onChange={(e) => handleNumberChange(e, true)}
//                     />
//                     {errors.buildingArea && <div style={errorText}>{errors.buildingArea}</div>}
//                 </div>
//             </div>
//
//             <div style={footerStyle}>
//                 <button
//                     type="button"
//                     style={btn(true, "secondary")}
//                     onClick={() => navigate(-1)}
//                 >
//                     취소
//                 </button>
//                 <button type="submit" style={btn(isFormValid && !saving)} disabled={!isFormValid || saving}>
//                     {saving ? (editMode ? "수정 중…" : "저장 중…") : (editMode ? "수정 저장" : "저장")}
//                 </button>
//             </div>
//         </form>
//     );
// }
//
// export default CreateSurvey;


// compact.css
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import axios from "axios";
import "./CreateSurvey.compact.css";

function CreateSurvey() {
    const navigate = useNavigate();
    const [sp] = useSearchParams();
    const editingId = sp.get("id");
    const editMode = useMemo(() => Boolean(editingId), [editingId]);

    const [saving, setSaving] = useState(false);
    const [loadingPrefill, setLoadingPrefill] = useState(false);
    const [formData, setFormData] = useState({
        lotAddress: "",
        latitude: "",
        longitude: "",
        buildingName: "",
        mainUseName: "",
        structureName: "",
        groundFloors: "",
        basementFloors: "",
        landArea: "",
        buildingArea: "",
    });
    const [errors, setErrors] = useState({});

    const hasValue = (v) =>
        v !== "" && v !== null && v !== undefined && String(v).trim() !== "";

    const fields = [
        { key: "lotAddress",     label: "번지주소",       type: "text",   placeholder: "예) 경상남도 김해시 ..." },
        { key: "latitude",       label: "위도",           type: "text",   placeholder: "예) 35.123456",      decimal: true },
        { key: "longitude",      label: "경도",           type: "text",   placeholder: "예) 128.123456",     decimal: true },
        { key: "buildingName",   label: "건물명",         type: "text",   placeholder: "예) 인수타워" },
        { key: "mainUseName",    label: "주용도",         type: "text",   placeholder: "예) 업무시설" },
        { key: "structureName",  label: "구조명",         type: "text",   placeholder: "예) 철근콘크리트구조" },
        { key: "groundFloors",   label: "지상층수",       type: "text",   placeholder: "예) 10",            decimal: false },
        { key: "basementFloors", label: "지하층수",       type: "text",   placeholder: "예) 2",             decimal: false },
        { key: "landArea",       label: "대지면적(㎡)",   type: "text",   placeholder: "예) 1234.56",       decimal: true },
        { key: "buildingArea",   label: "건축면적(㎡)",   type: "text",   placeholder: "예) 789.01",        decimal: true },
    ];

    const requiredAll = fields.map(f => f.key);

    const isFormValid = useMemo(() => {
        const allFilled = requiredAll.every((k) => hasValue(formData[k]));
        const noErrors = Object.values(errors).every((v) => !v);
        return allFilled && noErrors;
    }, [formData, errors]);

    const handleNumberChange = (e, allowDecimal = false) => {
        const { name, value } = e.target;
        const regex = allowDecimal ? /^\d*\.?\d*$/ : /^\d*$/;
        if (regex.test(value)) {
            setFormData((prev) => ({ ...prev, [name]: value }));
            setErrors((prev) => ({ ...prev, [name]: "" }));
        } else {
            setErrors((prev) => ({ ...prev, [name]: "숫자를 입력해주세요" }));
        }
    };

    const handleTextChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
        setErrors((prev) => ({ ...prev, [name]: "" }));
    };

    // 편집모드 프리필
    useEffect(() => {
        if (!editMode) return;
        (async () => {
            try {
                setLoadingPrefill(true);
                const { data } = await axios.get(`/web/building/${editingId}`);
                setFormData({
                    lotAddress:      data?.lotAddress ?? "",
                    latitude:        data?.latitude        != null ? String(data.latitude)        : "",
                    longitude:       data?.longitude       != null ? String(data.longitude)       : "",
                    buildingName:    data?.buildingName    ?? "",
                    mainUseName:     data?.mainUseName     ?? "",
                    structureName:   data?.structureName   ?? "",
                    groundFloors:    data?.groundFloors    != null ? String(data.groundFloors)    : "",
                    basementFloors:  data?.basementFloors  != null ? String(data.basementFloors)  : "",
                    landArea:        data?.landArea        != null ? String(data.landArea)        : "",
                    buildingArea:    data?.buildingArea    != null ? String(data.buildingArea)    : "",
                });
            } catch (e) {
                console.error(e);
                alert("대상 조사지(건물)를 불러올 수 없습니다.");
                navigate(-1);
            } finally {
                setLoadingPrefill(false);
            }
        })();
    }, [editMode, editingId, navigate]);

    // 전송 변환
    const toIntOrNull = (s) => (s === "" || s == null ? null : parseInt(s, 10));
    const toFloatOrNull = (s) => (s === "" || s == null ? null : parseFloat(s));
    const buildPayload = () => ({
        lotAddress: formData.lotAddress.trim(),
        latitude: toFloatOrNull(formData.latitude),
        longitude: toFloatOrNull(formData.longitude),
        buildingName: formData.buildingName.trim(),
        mainUseName: formData.mainUseName.trim(),
        structureName: formData.structureName.trim(),
        groundFloors: toIntOrNull(formData.groundFloors),
        basementFloors: toIntOrNull(formData.basementFloors),
        landArea: toFloatOrNull(formData.landArea),
        buildingArea: toFloatOrNull(formData.buildingArea),
    });

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!isFormValid) {
            alert("모든 필수 입력값을 올바르게 입력해 주세요.");
            return;
        }
        try {
            setSaving(true);
            const payload = buildPayload();
            if (editMode) {
                await axios.put(`/web/building/${editingId}`, payload);
                alert("수정되었습니다.");
            } else {
                await axios.post("/web/building", payload);
                alert("저장 성공");
            }
            navigate("/", { replace: true });
        } catch (error) {
            console.error("저장 중 오류 발생:", error);
            const msg = error?.response?.data?.message || error?.message || "저장 실패";
            alert(msg);
        } finally {
            setSaving(false);
        }
    };

    const cls = (k) => (errors[k] ? "cs-input is-invalid" : "cs-input");

    return (
        <form onSubmit={handleSubmit} className="cs-card" aria-busy={saving}>
            {/* 헤더 */}
            <div className="cs-header">
                <h2 className="cs-title">{editMode ? "조사목록 수정" : "조사목록 생성"}</h2>
                <p className="cs-sub">{loadingPrefill ? "기존 데이터를 불러오는 중…" : "모든 항목은 필수입니다."}</p>
            </div>

            {/* 본문: 3열 컴팩트 그리드 (스크롤 없이 한 화면에) */}
            <div className="cs-body">
                <div className="cs-grid">
                    {fields.map(({ key, label, type, placeholder, decimal }) => (
                        <div className="cs-field" key={key}>
                            <label htmlFor={key} className="cs-label">{label}</label>
                            <input
                                id={key}
                                name={key}
                                type={type}
                                className={cls(key)}
                                placeholder={placeholder}
                                value={formData[key]}
                                onChange={(e)=> decimal===undefined ? handleTextChange(e) : handleNumberChange(e, decimal)}
                                aria-invalid={!!errors[key]}
                            />
                            {errors[key] && <div className="cs-error">{errors[key]}</div>}
                        </div>
                    ))}
                </div>
            </div>

            {/* 액션 */}
            <div className="cs-actions">
                <button type="button" className="cs-btn cs-btn-secondary" onClick={()=>navigate(-1)}>
                    취소
                </button>
                <button type="submit" className="cs-btn cs-btn-primary" disabled={!isFormValid || saving}>
                    {saving ? (editMode ? "수정 중…" : "저장 중…") : (editMode ? "수정 저장" : "저장")}
                </button>
            </div>
        </form>
    );
}

export default CreateSurvey;
