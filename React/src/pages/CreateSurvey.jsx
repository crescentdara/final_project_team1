import { useState } from "react";
import axios from "axios";

function CreateSurvey() {
    const [formData, setFormData] = useState({
        lotAddress: "",
        latitude: "",
        longitude: "",
        buildingName: "",
        mainUse: "",
        structure: "",
        groundFloors: "",
        basementFloors: "",
        landArea: "",
        buildingArea: "",
    });

    const [step, setStep] = useState(1);
    const [errors, setErrors] = useState({});

    const requiredFields = {
        1: ["lotAddress", "latitude", "longitude"],
        2: ["buildingName", "mainUse", "structure", "groundFloors", "basementFloors"],
        3: ["landArea", "buildingArea"],
    };

    const isStepValid = () => {
        return requiredFields[step].every(
            (field) => formData[field] !== "" && !errors[field]
        );
    };

    const handleNumberChange = (e, allowDecimal = false) => {
        const { name, value } = e.target;
        const regex = allowDecimal ? /^\d*\.?\d*$/ : /^\d*$/;

        if (regex.test(value)) {
            setFormData({ ...formData, [name]: value });
            setErrors({ ...errors, [name]: "" });
        } else {
            setErrors({ ...errors, [name]: "숫자를 입력해주세요" });
        }
    };

    const handleSubmit = async () => {
        try {
            const response = await axios.post("/web/building", formData);
            alert("저장 성공");
            console.log("서버 응답:", response.data);
        } catch (error) {
            alert("저장 실패");
            console.error("저장 중 오류 발생:", error);
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
            <h2 style={{ textAlign: "center", marginBottom: "20px" }}>조사목록 생성</h2>
            <p style={{ textAlign: "center", color: "#666" }}>
                Step {step} / 3
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
                        onChange={(e) =>
                            setFormData({ ...formData, lotAddress: e.target.value })
                        }
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
                            style={buttonStyle(isStepValid())}
                            onClick={() => setStep(2)}
                            disabled={!isStepValid()}
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
                        onChange={(e) =>
                            setFormData({ ...formData, buildingName: e.target.value })
                        }
                    />

                    <label>주용도</label>
                    <input
                        style={inputStyle}
                        type="text"
                        name="mainUse"
                        value={formData.mainUse}
                        onChange={(e) =>
                            setFormData({ ...formData, mainUse: e.target.value })
                        }
                    />

                    <label>구조명</label>
                    <input
                        style={inputStyle}
                        type="text"
                        name="structure"
                        value={formData.structure}
                        onChange={(e) =>
                            setFormData({ ...formData, structure: e.target.value })
                        }
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
                            style={buttonStyle(isStepValid())}
                            onClick={handleSubmit}
                            disabled={!isStepValid()}
                        >
                            저장
                        </button>
                    </div>
                </div>
            )}

        </div>
    );
}

export default CreateSurvey;
