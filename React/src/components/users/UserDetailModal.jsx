// src/components/users/UserDetailModal.jsx
import React from "react";
import { Modal, Button, Spinner } from "react-bootstrap";
import UserDetailCard from "./UserDetailCard";
import AssignmentList from "./AssignmentList";

export default function UserDetailModal({
                                            show, onHide,
                                            detail, assignments,
                                            loadingDetail, loadingAssign
                                        }) {
    return (
        <Modal show={show} onHide={onHide} centered size="lg">
            <Modal.Header closeButton>
                <Modal.Title>조사원 상세 정보</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                {loadingDetail ? (
                    <div className="text-center my-4">
                        <Spinner animation="border" />
                    </div>
                ) : (
                    <UserDetailCard detail={detail} />
                )}

                <hr />

                <h5>배정된 건물</h5>
                {loadingAssign ? (
                    <div className="text-center my-4">
                        <Spinner animation="border" />
                    </div>
                ) : (
                    <AssignmentList items={assignments} />
                )}
            </Modal.Body>
            <Modal.Footer>
                <Button variant="secondary" onClick={onHide}>닫기</Button>
            </Modal.Footer>
        </Modal>
    );
}
