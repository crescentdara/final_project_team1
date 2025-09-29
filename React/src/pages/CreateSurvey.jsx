import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import axios from "axios";

function CreateSurvey() {
    const navigate = useNavigate();
    const [sp] = useSearchParams();
    const editingId = sp.get("id");                // ← ?id=123 이 있으면 편집모드
    const editMode = useMemo(() => Boolean(editingId), [editingId]);
    const [saving, setSaving] = useState(false);

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

    const [step, setStep] = useState(1);
    const [errors, setErrors] = useState({});
    const [loadingPrefill, setLoadingPrefill] = useState(false);

    const requiredFields = {
        1: ["lotAddress", "latitude", "longitude"],
        2: ["buildingName", "mainUseName", "structureName", "groundFloors", "basementFloors"],
        3: ["landArea", "buildingArea"],
    };

    const hasValue = (v) => v !== "" && v !== null && v !== undefined;

    const isStepValid = () =>
        requiredFields[step].every((field) => hasValue(formData[field]) && !errors[field]);

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

    // 편집모드: 프리필
    useEffect(() => {
        if (!editMode) return;
        (async () => {
            try {
                setLoadingPrefill(true);
                const { data } = await axios.get(`/web/building/${editingId}`);
                // 서버 엔티티 → 폼 키에 매핑
                setFormData({
                    lotAddress: data?.lotAddress ?? "",
                    latitude: data?.latitude ?? "",
                    longitude: data?.longitude ?? "",
                    buildingName: data?.buildingName ?? "",
                    mainUseName: data?.mainUseName ?? "",
                    structureName: data?.structureName ?? "",
                    groundFloors: data?.groundFloors ?? "",
                    basementFloors: data?.basementFloors ?? "",
                    landArea: data?.landArea ?? "",
                    buildingArea: data?.buildingArea ?? "",
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

    // 전송 전에 문자열 → 숫자/널 변환
    const toIntOrNull = (s) => (s === "" || s == null ? null : parseInt(s, 10));
    const toFloatOrNull = (s) => (s === "" || s == null ? null : parseFloat(s));

    const buildPayload = () => ({
        lotAddress: formData.lotAddress,
        latitude: toFloatOrNull(formData.latitude),
        longitude: toFloatOrNull(formData.longitude),
        buildingName: formData.buildingName,
        mainUseName: formData.mainUseName,
        structureName: formData.structureName,
        groundFloors: toIntOrNull(formData.groundFloors),
        basementFloors: toIntOrNull(formData.basementFloors),
        landArea: toFloatOrNull(formData.landArea),
        buildingArea: toFloatOrNull(formData.buildingArea),
    });

    const handleSubmit = async () => {
        // 마지막 스텝 유효성 체크(원하면 전체 스텝 검사로 확장 가능)
        if (!isStepValid()) {
            alert("필수 입력값을 확인해 주세요.");
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
            // 히스토리 대체 → 뒤로 가도 폼으로 안돌아옴
            navigate("/", { replace: true });
        } catch (error) {
            console.error("저장 중 오류 발생:", error);
            const msg = error?.response?.data?.message || error?.message || "저장 실패";
            alert(msg);
        } finally {
            setSaving(false);
        }
    };

    // 버튼 스타일
    const buttonStyle = (enabled) => ({
        padding: "10px 20px",
        borderRadius: "6px",
        border: "none",
        marginRight: "10px",
        cursor: enabled ? "pointer" : "not-allowed",
        backgroundColor: enabled ? "#289eff" : "#ccc",
        color: "white",
        fontWeight: "bold",
    });

    // input 스타일
    const inputStyle = {
        width: "100%",
        padding: "8px",
        marginTop: "5px",
        marginBottom: "10px",
        borderRadius: "4px",
        border: "1px solid #ccc",
    };

    // card 스타일
    const cardStyle = {
        maxWidth: "500px",
        margin: "30px auto",
        padding: "20px",
        borderRadius: "12px",
        boxShadow: "0 4px 10px rgba(0,0,0,0.1)",
        background: "white",
    };

    return (
        <div style={cardStyle}>
            <h2 style={{ textAlign: "center", marginBottom: "8px" }}>
                {editMode ? "조사목록 수정" : "조사목록 생성"}
            </h2>
            <p style={{ textAlign: "center", color: "#666", marginBottom: 16 }}>
                {loadingPrefill ? "기존 데이터를 불러오는 중…" : `Step ${step} / 3`}
            </p>

            {/* Step 1 */}
            {step === 1 && (
                <div>
                    <h3>위치 정보</h3>

                    <label>번지주소</label>
                    <input
                        style={inputStyle}
                        type="text"
                        name="lotAddress"
                        value={formData.lotAddress}
                        onChange={(e) => setFormData({ ...formData, lotAddress: e.target.value })}
                    />

                    <label>위도</label>
                    <input
                        style={inputStyle}
                        type="text"
                        name="latitude"
                        value={formData.latitude}
                        onChange={(e) => handleNumberChange(e, true)}
                    />
                    {errors.latitude && (
                        <div style={{ color: "red", fontSize: "12px", marginTop: "-8px", marginBottom: "10px" }}>
                            {errors.latitude}
                        </div>
                    )}

                    <label>경도</label>
                    <input
                        style={inputStyle}
                        type="text"
                        name="longitude"
                        value={formData.longitude}
                        onChange={(e) => handleNumberChange(e, true)}
                    />
                    {errors.longitude && (
                        <div style={{ color: "red", fontSize: "12px", marginTop: "-8px", marginBottom: "10px" }}>
                            {errors.longitude}
                        </div>
                    )}

                    <div style={{ textAlign: "right" }}>
                        <button
                            style={buttonStyle(isStepValid() && !loadingPrefill)}
                            onClick={() => setStep(2)}
                            disabled={!isStepValid() || loadingPrefill}
                        >
                            다음
                        </button>
                    </div>
                </div>
            )}

            {/* Step 2 */}
            {step === 2 && (
                <div>
                    <h3>건물 정보</h3>

                    <label>건물명</label>
                    <input
                        style={inputStyle}
                        type="text"
                        name="buildingName"
                        value={formData.buildingName}
                        onChange={(e) => setFormData({ ...formData, buildingName: e.target.value })}
                    />

                    <label>주용도</label>
                    <input
                        style={inputStyle}
                        type="text"
                        name="mainUseName"
                        value={formData.mainUseName}
                        onChange={(e) => setFormData({ ...formData, mainUseName: e.target.value })}
                    />

                    <label>구조명</label>
                    <input
                        style={inputStyle}
                        type="text"
                        name="structureName"
                        value={formData.structureName}
                        onChange={(e) => setFormData({ ...formData, structureName: e.target.value })}
                    />

                    <label>지상층수</label>
                    <input
                        style={inputStyle}
                        type="text"
                        name="groundFloors"
                        value={formData.groundFloors}
                        onChange={(e) => handleNumberChange(e, false)}
                    />
                    {errors.groundFloors && (
                        <div style={{ color: "red", fontSize: "12px", marginTop: "-8px", marginBottom: "10px" }}>
                            {errors.groundFloors}
                        </div>
                    )}

                    <label>지하층수</label>
                    <input
                        style={inputStyle}
                        type="text"
                        name="basementFloors"
                        value={formData.basementFloors}
                        onChange={(e) => handleNumberChange(e, false)}
                    />
                    {errors.basementFloors && (
                        <div style={{ color: "red", fontSize: "12px", marginTop: "-8px", marginBottom: "10px" }}>
                            {errors.basementFloors}
                        </div>
                    )}

                    <div style={{ textAlign: "right" }}>
                        <button style={buttonStyle(true)} onClick={() => setStep(1)}>
                            이전
                        </button>
                        <button
                            style={buttonStyle(isStepValid())}
                            onClick={() => setStep(3)}
                            disabled={!isStepValid()}
                        >
                            다음
                        </button>
                    </div>
                </div>
            )}

            {/* Step 3 */}
            {step === 3 && (
                <div>
                    <h3>면적 정보</h3>

                    <label>대지면적</label>
                    <input
                        style={inputStyle}
                        type="text"
                        name="landArea"
                        value={formData.landArea}
                        onChange={(e) => handleNumberChange(e, true)}
                    />
                    {errors.landArea && (
                        <div style={{ color: "red", fontSize: "12px", marginTop: "-8px", marginBottom: "10px" }}>
                            {errors.landArea}
                        </div>
                    )}

                    <label>건축면적</label>
                    <input
                        style={inputStyle}
                        type="text"
                        name="buildingArea"
                        value={formData.buildingArea}
                        onChange={(e) => handleNumberChange(e, true)}
                    />
                    {errors.buildingArea && (
                        <div style={{ color: "red", fontSize: "12px", marginTop: "-8px", marginBottom: "10px" }}>
                            {errors.buildingArea}
                        </div>
                    )}

                    <div style={{ textAlign: "right" }}>
                        <button style={buttonStyle(true)} onClick={() => setStep(2)}>
                            이전
                        </button>
                        <button
                            style={buttonStyle(isStepValid() && !saving)}
                            onClick={handleSubmit}
                            disabled={!isStepValid() || saving}
                        >
                            {saving ? (editMode ? "수정 중…" : "저장 중…") : (editMode ? "수정 저장" : "저장")}
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}

export default CreateSurvey;
