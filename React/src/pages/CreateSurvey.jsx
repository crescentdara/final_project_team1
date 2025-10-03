import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import axios from "axios";

function CreateSurvey({ editingId: propId }) {
    const navigate = useNavigate();
    const [sp] = useSearchParams();
    const queryId = sp.get("id");
    const editingId = propId || queryId; // ✅ props 우선, 없으면 query 사용
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

    // ---------- helpers ----------
    const hasValue = (v) =>
        v !== "" && v !== null && v !== undefined && String(v).trim() !== "";

    const requiredAll = [
        "lotAddress",
        "latitude",
        "longitude",
        "buildingName",
        "mainUseName",
        "structureName",
        "groundFloors",
        "basementFloors",
        "landArea",
        "buildingArea",
    ];

    const isFormValid = useMemo(() => {
        const allFilled = requiredAll.every((k) => hasValue(formData[k]));
        const noErrors = Object.values(errors).every((v) => !v);
        return allFilled && noErrors;
    }, [formData, errors]);

    // 숫자 핸들러
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

    // 일반 텍스트 핸들러
    const handleTextChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
        setErrors((prev) => ({ ...prev, [name]: "" }));
    };

    // 편집모드: 프리필
    useEffect(() => {
        if (!editMode) return;
        (async () => {
            try {
                setLoadingPrefill(true);
                const { data } = await axios.get(`/web/building/${editingId}`);
                setFormData({
                    lotAddress: data?.lotAddress ?? "",
                    latitude: data?.latitude != null ? String(data.latitude) : "",
                    longitude: data?.longitude != null ? String(data.longitude) : "",
                    buildingName: data?.buildingName ?? "",
                    mainUseName: data?.mainUseName ?? "",
                    structureName: data?.structureName ?? "",
                    groundFloors:
                        data?.groundFloors != null ? String(data.groundFloors) : "",
                    basementFloors:
                        data?.basementFloors != null ? String(data.basementFloors) : "",
                    landArea: data?.landArea != null ? String(data.landArea) : "",
                    buildingArea:
                        data?.buildingArea != null ? String(data.buildingArea) : "",
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

    // 숫자 변환
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
            const msg =
                error?.response?.data?.message || error?.message || "저장 실패";
            alert(msg);
        } finally {
            setSaving(false);
        }
    };

    // ---------- compact styles & responsive grid ----------
    const borderColorBase = "#d0d7de";

    const formStyle = {
        width: "100%",
        height: parent,
        margin: 0,
    };

    // 섹션 타이틀 간격 축소
    const sectionTitleCompact = {
        marginTop: 14,
        marginBottom: 8,
        fontSize: 16,
        fontWeight: 700,
        borderBottom: "2px solid #f0f0f0",
        paddingBottom: 6,
        color: "#333",
    };

    // 더 작은 입력/라벨
    const labelSm = { fontSize: 13, fontWeight: 600, marginBottom: 4 };
    const inputSm = {
        width: "100%",
        padding: "8px 10px",
        borderRadius: 8,
        border: `1px solid ${borderColorBase}`,
        outline: "none",
        fontSize: 13,
        lineHeight: "1.2",
        height: 36,
    };
    const errorText = { color: "#d93025", fontSize: 12, marginTop: 4 };

    const footerStyle = {
        marginTop: 24,
        display: "flex",
        justifyContent: "flex-end",
        gap: 10,
    };
    const btn = (enabled, variant = "primary") => ({
        minWidth: 92,
        padding: "10px 16px",
        borderRadius: 10,
        border: "none",
        cursor: enabled ? "pointer" : "not-allowed",
        fontWeight: 600,
        fontSize: 14,
        background:
            variant === "secondary"
                ? enabled
                    ? "#6c757d"
                    : "#a6acb1"
                : enabled
                    ? "#289eff"
                    : "#bcdcff",
        color: "white",
    });

    // 반응형: 화면 폭 감지
    const [vw, setVw] = useState(
        typeof window !== "undefined" ? window.innerWidth : 1920
    );
    useEffect(() => {
        const onResize = () => setVw(window.innerWidth);
        window.addEventListener("resize", onResize);
        return () => window.removeEventListener("resize", onResize);
    }, []);
    const colCount = vw >= 1200 ? 3 : vw >= 720 ? 2 : 1;

    const fieldWrapAuto =
        colCount === 1
            ? { display: "grid", gridTemplateColumns: "1fr", gap: "12px" }
            : {
                display: "grid",
                gridTemplateColumns: `repeat(${colCount}, 1fr)`,
                gap: colCount >= 3 ? "12px 16px" : "16px 20px",
            };

    // 위치정보 전용: "번지주소 2fr + 위도 1fr + 경도 1fr" (넓을 때)
    const fieldWrapLocation =
        colCount >= 3
            ? {
                display: "grid",
                gridTemplateColumns: "1.5fr 1fr 1fr",
                gap: "12px 16px",
            }
            : fieldWrapAuto;

    return (
        <form onSubmit={handleSubmit} style={{formStyle, height: "100%", alignItems: "stretch" }}>
            {/* 위치 정보 */}
            <div style={sectionTitleCompact}>위치 정보</div>
            <div style={fieldWrapLocation}>
                {/* lotAddress */}
                <div>
                    <div style={labelSm}>번지주소</div>
                    <input
                        style={{
                            ...inputSm,
                            borderColor: errors.lotAddress ? "#d93025" : borderColorBase,
                        }}
                        type="text"
                        name="lotAddress"
                        placeholder="예) 경상남도 김해시 ..."
                        value={formData.lotAddress}
                        onChange={handleTextChange}
                    />
                    {errors.lotAddress && (
                        <div style={errorText}>{errors.lotAddress}</div>
                    )}
                </div>

                {/* latitude */}
                <div>
                    <div style={labelSm}>위도</div>
                    <input
                        style={{
                            ...inputSm,
                            borderColor: errors.latitude ? "#d93025" : borderColorBase,
                        }}
                        type="text"
                        name="latitude"
                        placeholder="예) 35.123456"
                        value={formData.latitude}
                        onChange={(e) => handleNumberChange(e, true)}
                    />
                    {errors.latitude && <div style={errorText}>{errors.latitude}</div>}
                </div>

                {/* longitude */}
                <div>
                    <div style={labelSm}>경도</div>
                    <input
                        style={{
                            ...inputSm,
                            borderColor: errors.longitude ? "#d93025" : borderColorBase,
                        }}
                        type="text"
                        name="longitude"
                        placeholder="예) 128.123456"
                        value={formData.longitude}
                        onChange={(e) => handleNumberChange(e, true)}
                    />
                    {errors.longitude && <div style={errorText}>{errors.longitude}</div>}
                </div>
            </div>

            {/* 건물 정보 */}
            <div style={sectionTitleCompact}>건물 정보</div>
            <div style={fieldWrapAuto}>
                {/* buildingName */}
                <div>
                    <div style={labelSm}>건물명</div>
                    <input
                        style={{
                            ...inputSm,
                            borderColor: errors.buildingName ? "#d93025" : borderColorBase,
                        }}
                        type="text"
                        name="buildingName"
                        placeholder="예) 인수타워"
                        value={formData.buildingName}
                        onChange={handleTextChange}
                    />
                    {errors.buildingName && (
                        <div style={errorText}>{errors.buildingName}</div>
                    )}
                </div>

                {/* mainUseName */}
                <div>
                    <div style={labelSm}>주용도</div>
                    <input
                        style={{
                            ...inputSm,
                            borderColor: errors.mainUseName ? "#d93025" : borderColorBase,
                        }}
                        type="text"
                        name="mainUseName"
                        placeholder="예) 업무시설"
                        value={formData.mainUseName}
                        onChange={handleTextChange}
                    />
                    {errors.mainUseName && (
                        <div style={errorText}>{errors.mainUseName}</div>
                    )}
                </div>

                {/* structureName */}
                <div>
                    <div style={labelSm}>구조명</div>
                    <input
                        style={{
                            ...inputSm,
                            borderColor: errors.structureName ? "#d93025" : borderColorBase,
                        }}
                        type="text"
                        name="structureName"
                        placeholder="예) 철근콘크리트구조"
                        value={formData.structureName}
                        onChange={handleTextChange}
                    />
                    {errors.structureName && (
                        <div style={errorText}>{errors.structureName}</div>
                    )}
                </div>

                {/* groundFloors */}
                <div>
                    <div style={labelSm}>지상층수</div>
                    <input
                        style={{
                            ...inputSm,
                            borderColor: errors.groundFloors ? "#d93025" : borderColorBase,
                        }}
                        type="text"
                        name="groundFloors"
                        placeholder="예) 10"
                        value={formData.groundFloors}
                        onChange={(e) => handleNumberChange(e, false)}
                    />
                    {errors.groundFloors && (
                        <div style={errorText}>{errors.groundFloors}</div>
                    )}
                </div>

                {/* basementFloors */}
                <div>
                    <div style={labelSm}>지하층수</div>
                    <input
                        style={{
                            ...inputSm,
                            borderColor: errors.basementFloors
                                ? "#d93025"
                                : borderColorBase,
                        }}
                        type="text"
                        name="basementFloors"
                        placeholder="예) 2"
                        value={formData.basementFloors}
                        onChange={(e) => handleNumberChange(e, false)}
                    />
                    {errors.basementFloors && (
                        <div style={errorText}>{errors.basementFloors}</div>
                    )}
                </div>
            </div>

            {/* 면적 정보 */}
            <div style={sectionTitleCompact}>면적 정보</div>
            <div style={fieldWrapAuto}>
                {/* landArea */}
                <div>
                    <div style={labelSm}>대지면적(㎡)</div>
                    <input
                        style={{
                            ...inputSm,
                            borderColor: errors.landArea ? "#d93025" : borderColorBase,
                        }}
                        type="text"
                        name="landArea"
                        placeholder="예) 1234.56"
                        value={formData.landArea}
                        onChange={(e) => handleNumberChange(e, true)}
                    />
                    {errors.landArea && <div style={errorText}>{errors.landArea}</div>}
                </div>

                {/* buildingArea */}
                <div>
                    <div style={labelSm}>건축면적(㎡)</div>
                    <input
                        style={{
                            ...inputSm,
                            borderColor: errors.buildingArea ? "#d93025" : borderColorBase,
                        }}
                        type="text"
                        name="buildingArea"
                        placeholder="예) 789.01"
                        value={formData.buildingArea}
                        onChange={(e) => handleNumberChange(e, true)}
                    />
                    {errors.buildingArea && (
                        <div style={errorText}>{errors.buildingArea}</div>
                    )}
                </div>
            </div>

            {/* 액션 */}
            <div style={footerStyle}>
                <button
                    type="button"
                    style={btn(true, "secondary")}
                    onClick={() => navigate(-1)}
                >
                    취소
                </button>
                <button
                    type="submit"
                    style={btn(isFormValid && !saving)}
                    disabled={!isFormValid || saving}
                >
                    {saving ? (editMode ? "수정 중…" : "저장 중…") : editMode ? "수정 저장" : "저장"}
                </button>
            </div>
        </form>
    );
}

export default CreateSurvey;
