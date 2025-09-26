// src/components/users/UserDetailModal.jsx
import React, { useState, useEffect } from "react";
import { Modal, Button, Spinner } from "react-bootstrap";
import axios from "axios";
import UserDetailCard from "./UserDetailCard";
import AssignmentList from "./AssignmentList";
import UserEditForm from "./UserEditForm";
import Pagination from "../ui/Pagination.jsx";
import { useNavigate } from "react-router-dom";

export default function UserDetailModal({
                                            show,
                                            onHide,
                                            detail,
                                            assignments,
                                            loadingDetail,
                                            loadingAssign,
                                        }) {
    const [isEditMode, setIsEditMode] = useState(false);

    const [page, setPage] = useState(1);
    const size = 10;
    const [total, setTotal] = useState(0);

    // ✅ assignments 변경될 때 total 세팅
    useEffect(() => {
        if (assignments) {
            setTotal(assignments.length);
        }
    }, [assignments]);

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
                            // ✅ 현재 페이지에 맞는 데이터만 slice 해서 전달
                            <AssignmentList
                                items={assignments.slice(
                                    (page - 1) * size,
                                    page * size
                                )}
                            />
                        )}
                    </>
                )}
            </Modal.Body>

            <Pagination
                page={page}
                total={total}
                size={size}
                onChange={setPage}
                siblings={1}
                boundaries={1}
                className="justify-content-center"
                lastAsLabel={false}
            />

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
                            style={{
                                backgroundColor: "#289eff",
                                borderColor: "#289eff",
                            }}
                            onClick={() =>
                                document.querySelector("form")?.requestSubmit()
                            }
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
                            style={{
                                backgroundColor: "#289eff",
                                borderColor: "#289eff",
                            }}
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
