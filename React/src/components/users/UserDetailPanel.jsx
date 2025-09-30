import React, { useState, useEffect } from "react";
import axios from "axios";
import UserDetailCard from "./UserDetailCard";
import AssignmentList from "./AssignmentList";
import UserEditForm from "./UserEditForm";
import Pagination from "../ui/Pagination.jsx";

export default function UserDetailPanel({
                                            isOpen,
                                            onClose,
                                            detail,
                                            assignments,
                                            loadingDetail,
                                            loadingAssign,
                                        }) {
    const [isEditMode, setIsEditMode] = useState(false);
    const [page, setPage] = useState(1);
    const size = 10;
    const [total, setTotal] = useState(0);

    // 닫힘 애니메이션 지원을 위한 상태
    const [visible, setVisible] = useState(isOpen);

    useEffect(() => {
        if (isOpen) {
            setVisible(true); // 열릴 때는 바로 보이게
        } else {
            // 닫힐 때는 transition 끝난 뒤에 제거
            const timer = setTimeout(() => setVisible(false), 300); // transition 시간(0.3s)과 맞춤
            return () => clearTimeout(timer);
        }
    }, [isOpen]);

    // 완전히 닫힌 상태라면 아예 렌더링 안 함
    if (!visible) return null;

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
            onClose();
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
            onClose();
            window.location.reload();
        } catch (err) {
            console.error(err);
            alert("수정 실패: " + err.message);
        }
    };

    return (
        <div
            style={{
                width: isOpen ? "40%" : "0",
                minWidth: isOpen ? "400px" : "0",
                background: "#fff",
                boxShadow: "-2px 0 8px rgba(0,0,0,0.08)",
                borderLeft: "1px solid #eee",
                transition: "all 0.3s ease",
                overflow: "hidden",
                // marginTop: "20px",
                // marginBottom: "20px",
                padding: isOpen ? "20px" : "0",
                borderRadius: "12px 0 0 12px",
            }}
        >
            {/* 헤더 */}
            <div
                className="d-flex justify-content-between align-items-center mb-3"
                style={{ borderBottom: "1px solid #f0f0f0", paddingBottom: "10px" }}
            >
                <h5 className="m-0">
                    {isEditMode ? "조사원 정보 수정" : "조사원 상세 정보"}
                </h5>
                <button type="button" className="btn-close" onClick={onClose}></button>
            </div>

            {/* 바디 */}
            {loadingDetail ? (
                <p className="text-muted">로딩중…</p>
            ) : isEditMode ? (
                <UserEditForm
                    detail={detail}
                    onSave={handleSave}
                    onCancel={() => setIsEditMode(false)}
                />
            ) : (
                <UserDetailCard detail={detail} />
            )}

            {/* ✅ 배정된 건물 리스트 */}
            {!isEditMode && (
                <>
                    <hr />
                    <h6 className="fw-semibold">배정된 건물</h6>
                    {loadingAssign ? (
                        <p className="text-muted">로딩중…</p>
                    ) : (
                        <AssignmentList
                            items={(assignments ?? []).slice((page - 1) * size, page * size)}
                        />
                    )}
                </>
            )}

            {/* ✅ 페이지네이션 */}
            {!isEditMode && !loadingAssign && total > size && (
                <Pagination
                    page={page}
                    total={total}
                    size={size}
                    onChange={setPage}
                    siblings={1}
                    boundaries={1}
                    className="justify-content-center mt-3"
                    lastAsLabel={false}
                />
            )}

            {/* 푸터 */}
            <div className="d-flex justify-content-end gap-2 mt-3">
                {isEditMode ? (
                    <>
                        <button
                            className="btn btn-secondary"
                            onClick={() => setIsEditMode(false)}
                        >
                            취소
                        </button>
                        <button
                            className="btn btn-primary"
                            onClick={() =>
                                document.querySelector("form")?.requestSubmit()
                            }
                        >
                            저장
                        </button>
                    </>
                ) : (
                    <>
                        <button
                            className="btn btn-primary"
                            onClick={() => setIsEditMode(true)}
                        >
                            수정
                        </button>
                        <button className="btn btn-danger" onClick={handleDelete}>
                            삭제
                        </button>
                    </>
                )}
            </div>
        </div>
    );
}
