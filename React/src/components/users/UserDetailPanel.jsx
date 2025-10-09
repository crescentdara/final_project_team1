import React, {useState, useEffect} from "react";
import axios from "axios";
// 상세 테이블은 제거된 상태 유지
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
                                            // ✅ 추가: 행의 “수정” 클릭 시 수정 모드로 바로 열기
                                            startEdit = false,
                                            // ✅ 추가: 저장/삭제 후 목록 갱신을 부모가 처리
                                            onSaved,
                                            onDeleted,
                                        }) {
    const [isEditMode, setIsEditMode] = useState(false);
    const [page, setPage] = useState(1);
    const size = 10;
    const [total, setTotal] = useState(0);

    // 닫힘 애니메이션 지원
    const [visible, setVisible] = useState(isOpen);
    useEffect(() => {
        if (isOpen) {
            setVisible(true);
        } else {
            const timer = setTimeout(() => setVisible(false), 300);
            return () => clearTimeout(timer);
        }
    }, [isOpen]);

    // ✅ 패널이 열릴 때 startEdit이면 즉시 수정모드
    useEffect(() => {
        if (isOpen && startEdit) setIsEditMode(true);
    }, [isOpen, startEdit]);

    if (!visible) return null;

    // assignments 변경 시 페이지/총건수 갱신
    useEffect(() => {
        if (Array.isArray(assignments)) {
            setTotal(assignments.length);
            setPage(1);
        }
    }, [assignments]);

    /** 삭제 처리 */
    const handleDelete = async () => {
        if (!detail?.userId) return;
        if (!window.confirm("정말 삭제하시겠습니까?")) return;

        try {
            await axios.delete(`/web/api/users/${detail.userId}`);
            alert("삭제 완료");
            onDeleted?.(); // 부모에서 리스트 갱신 + 선택 해제
            onClose();
        } catch (err) {
            console.error(err);
            alert("삭제 실패: " + (err?.response?.data?.message || err?.message || ""));
        }
    };

    /** 수정 저장 처리 */
    const handleSave = async (formData) => {
        try {
            await axios.put(`/web/api/users/${detail.userId}`, formData);
            alert("수정 완료");
            setIsEditMode(false);
            onSaved?.(); // 부모에서 리스트 갱신
        } catch (err) {
            console.error(err);
            alert("수정 실패: " + (err?.response?.data?.message || err?.message || ""));
        }
    };

    const nameOrFallback =
        (detail?.name && detail.name.trim()) ||
        detail?.username ||
        (detail?.userId ? `ID: ${detail.userId}` : "조사원");

    const headerTitle = isEditMode ? `${nameOrFallback} 정보 수정` : nameOrFallback;

    return (
        <div
            style={{
                width: isOpen ? "400px" : "0",
                minWidth: isOpen ? "400px" : "0",
                height: "100%",
                background: "#fff",
                boxShadow: "-2px 0 8px rgba(0,0,0,0.08)",
                borderLeft: "1px solid #eee",
                transition: "all 0.3s ease",
                overflow: "hidden",
                padding: isOpen ? "20px" : "0",
                borderRadius: "12px",
            }}
        >
            {/* 헤더 */}
            <div
                className="d-flex justify-content-between align-items-center mb-3"
            >
                <h5 className="m-0" aria-live="polite">{headerTitle}</h5>
                <button type="button" className="btn-close" onClick={onClose}/>
            </div>

            {/* 바디 */}
            {isEditMode ? (
                // ✅ 수정 모드에서만 폼 표시
                <UserEditForm
                    detail={detail}
                    onSave={handleSave}
                    onCancel={() => setIsEditMode(false)}
                />
            ) : (
                // ✅ 기본 모드에서는 상세 테이블을 표시하지 않음(요구 사항)
                <></>
            )}

            {/* 배정된 건물 */}
            {!isEditMode && (
                <>
                    <hr/>
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

            {/* 페이지네이션 */}
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

            {/* 푸터: 패널 내부에서도 수정/삭제 가능 */}
            <div className="d-flex justify-content-end gap-2 mt-3">
                {isEditMode ? (
                    <>
                        <button className="btn btn-secondary" onClick={() => setIsEditMode(false)}>
                            취소
                        </button>
                        <button
                            className="btn btn-primary"
                            onClick={() => document.querySelector("form")?.requestSubmit()}
                            disabled={loadingDetail}
                        >
                            저장
                        </button>
                    </>
                ) : (
                    <>

                    </>
                )}
            </div>
        </div>
    );
}
