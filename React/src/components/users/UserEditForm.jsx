// src/components/users/UserEditForm.jsx
import React, { useState, useEffect } from "react";
import { Form, Button } from "react-bootstrap";
import axios from "axios";

export default function UserEditForm({ detail, onSave}) {
    const [formData, setFormData] = useState({
        name: "",
        username: "",
        preferredRegion: "",
        role: "RESEARCHER",
    });

    const [regions, setRegions] = useState([]);

    /** ✅ 초기값 세팅 */
    useEffect(() => {
        if (detail) {
            setFormData({
                name: detail.name || "",
                username: detail.username || "",
                preferredRegion: detail.preferredRegion || "",
                role: detail.role || "RESEARCHER",
            });
        }
    }, [detail]);

    /** ✅ 선호 지역 목록 불러오기 */
    useEffect(() => {
        axios
            .get("/web/api/users/preferred-regions?city=김해시")
            .then((res) => setRegions(res.data))
            .catch((err) => console.error("선호지역 불러오기 실패:", err));
    }, []);

    /** ✅ input 변경 핸들러 */
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    /** ✅ 저장 */
    const handleSubmit = (e) => {
        e.preventDefault();
        onSave(formData);
    };

    return (
        <Form onSubmit={handleSubmit}>
            <Form.Group className="mb-3">
                <hr />
                <Form.Label>이름</Form.Label>
                <Form.Control
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    required
                />
            </Form.Group>

            <Form.Group className="mb-3">
                <Form.Label>아이디</Form.Label>
                <Form.Control
                    type="text"
                    name="username"
                    value={formData.username}
                    onChange={handleChange}
                    required
                />
            </Form.Group>

            <Form.Group className="mb-3">
                <Form.Label>선호 지역</Form.Label>
                <Form.Select
                    name="preferredRegion"
                    value={formData.preferredRegion}
                    onChange={handleChange}
                >
                    <option value="">선택</option>
                    {regions.map((region, idx) => (
                        <option key={idx} value={region}>
                            {region}
                        </option>
                    ))}
                </Form.Select>
            </Form.Group>

        </Form>
    );
}
