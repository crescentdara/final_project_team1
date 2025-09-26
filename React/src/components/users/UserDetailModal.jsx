// src/components/users/UserDetailModal.jsx
import React, { useState } from "react";
import { Modal, Button, Spinner } from "react-bootstrap";
import axios from "axios";
import UserDetailCard from "./UserDetailCard";
import AssignmentList from "./AssignmentList";
import UserEditForm from "./UserEditForm";

export default function UserDetailModal({
                                            show,
                                            onHide,
                                            detail,
                                            assignments,
                                            loadingDetail,
                                            loadingAssign,
                                        }) {
    const [isEditMode, setIsEditMode] = useState(false);

    /** 삭제 처리 */
    const handleDelete = async () => {
        if (!detail?.userId) return;
        if (!window.confirm("정말 삭제하시겠습니까?")) return;

        try {
            await axios.delete(`/web/api/users/${detail.userId}`);
            alert("삭제 완료");
            onHide();
            window.location.reload();
        } catch (err) {
            console.error(err);
            alert("삭제 실패: " + err.message);
        }
    };

    /** 수정 저장 처리 */
    const handleSave = async (formData) => {
        try {
            await axios.put(`/web/api/users/${detail.userId}`, formData);
            alert("수정 완료");
            setIsEditMode(false);
            onHide();
            window.location.reload();
        } catch (err) {
            console.error(err);
            alert("수정 실패: " + err.message);
        }
    };

    return (
        <Modal show={show} onHide={onHide} centered size="lg">
            <Modal.Header closeButton>
                <Modal.Title>
                    {isEditMode ? "조사원 정보 수정" : "조사원 상세 정보"}
                </Modal.Title>
            </Modal.Header>

            <Modal.Body>
                {loadingDetail ? (
                    <div className="text-center my-4">
                        <Spinner animation="border" />
                    </div>
                ) : isEditMode ? (
                    <UserEditForm
                        detail={detail}
                        onSave={handleSave}
                        onCancel={() => setIsEditMode(false)}
                    />
                ) : (
                    <UserDetailCard detail={detail} />
                )}

                {!isEditMode && (
                    <>
                        <hr />
                        <h5>배정된 건물</h5>
                        {loadingAssign ? (
                            <div className="text-center my-4">
                                <Spinner animation="border" />
                            </div>
                        ) : (
                            <AssignmentList items={assignments} />
                        )}
                    </>
                )}
            </Modal.Body>

            <Modal.Footer>
                {isEditMode ? (
                    <>
                        <Button
                            variant="secondary"
                            onClick={() => setIsEditMode(false)}
                        >
                            취소
                        </Button>
                        <Button
                            style={{ backgroundColor: "#289eff", borderColor: "#289eff" }}
                            onClick={() => document.querySelector("form")?.requestSubmit()}
                        >
                            저장
                        </Button>
                    </>
                ) : (
                    <>
                        <Button variant="danger" onClick={handleDelete}>
                            삭제
                        </Button>
                        <Button
                            style={{ backgroundColor: "#289eff", borderColor: "#289eff" }}
                            onClick={() => setIsEditMode(true)}
                        >
                            수정
                        </Button>
                        <Button variant="secondary" onClick={onHide}>
                            닫기
                        </Button>
                    </>
                )}
            </Modal.Footer>
        </Modal>
    );
}
