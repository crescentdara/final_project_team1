import React, { useEffect, useState } from "react";
import ReportPdfModal from "../components/modals/ReportPdfModal.jsx";
import Pagination from "../components/ui/Pagination.jsx";

/** 결과 보고서 필터 영역 */
function ReportFilters({ keyword, setKeyword, sort, setSort, onSearch }) {
    return (
        <div className="d-flex flex-wrap gap-2 align-items-center mb-3">
            <h3 className="m-0 me-auto">결과 보고서</h3>

            <select
                className="form-select"
                style={{ maxWidth: 160 }}
                value={sort}
                onChange={(e) => setSort(e.target.value)}
            >
                <option value="latest">최신 생성순</option>
                <option value="oldest">오래된 순</option>
            </select>

            <div className="input-group" style={{ maxWidth: 360 }}>
                <input
                    className="form-control"
                    placeholder="관리번호 / 조사원 / 주소 검색"
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                    onKeyDown={(e) => e.key === "Enter" && onSearch()}
                />
                <button className="btn btn-outline-secondary" onClick={onSearch}>
                    검색
                </button>
            </div>
        </div>
    );
}

/** 결과 보고서 리스트 아이템 */
function ReportItem({ report, onOpen }) {
    return (
        <div className="card shadow-sm mb-3 border-0 rounded-3">
            <div className="card-body d-flex justify-content-between align-items-center">
                <div>
                    <div className="fw-semibold">
                        {report.caseNo} · {report.investigator}
                    </div>
                    <div className="text-muted small mt-1">{report.address}</div>
                </div>
                <button className="btn btn-primary" onClick={() => onOpen(report.id)}>
                    보고서 보기
                </button>
            </div>
        </div>
    );
}

export default function ResultReport() {
    const [reports, setReports] = useState([]);
    const [keyword, setKeyword] = useState("");
    const [sort, setSort] = useState("latest");

    // 📌 페이지네이션 상태
    const [page, setPage] = useState(1); // 1-based
    const [total, setTotal] = useState(0);
    const size = 10; // ✅ 한 페이지당 개수 (통일)

    // 📌 모달 상태
    const [modalOpen, setModalOpen] = useState(false);
    const [selectedReportId, setSelectedReportId] = useState(null);

    const fetchReports = () => {
        const params = new URLSearchParams({
            keyword: keyword,
            sort: sort,
            page: page - 1, // 백엔드는 0-based
            size: size,
        });

        fetch(`/web/api/report?${params.toString()}`)
            .then((res) => {
                if (!res.ok) throw new Error("보고서 목록 불러오기 실패");
                return res.json();
            })
            .then((data) => {
                // Spring Data Page 응답 가정: { content, totalPages, totalElements ... }
                setReports(data.content || []);
                setTotal(data.totalElements || 0); // ✅ totalElements 반영
            })
            .catch((e) => console.error(e));
    };

    useEffect(() => {
        fetchReports();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [page, sort]);

    const handleOpen = (id) => {
        setSelectedReportId(id);
        setModalOpen(true);
    };

    return (
        <div className="container py-4">
            {/* 필터 */}
            <ReportFilters
                keyword={keyword}
                setKeyword={setKeyword}
                sort={sort}
                setSort={setSort}
                onSearch={() => {
                    setPage(1); // 검색 시 페이지 초기화
                    fetchReports();
                }}
            />

            {/* 리스트 */}
            {reports.length === 0 ? (
                <div className="text-center text-muted py-5 border rounded-4">
                    표시할 보고서가 없습니다.
                </div>
            ) : (
                reports.map((r) => (
                    <ReportItem key={r.id} report={r} onOpen={handleOpen} />
                ))
            )}

            {/* 페이지네이션 */}
            <Pagination
                page={page}
                total={total}      // 전체 데이터 개수
                size={size}        // 한 페이지당 개수
                onChange={setPage}
                siblings={1}       // 현재 페이지 양옆 1개씩 표시
                boundaries={1}     // 처음/끝 경계 1개 유지
                className="justify-content-center"
                lastAsLabel={false}
            />

            {/* PDF 모달 */}
            {modalOpen && (
                <ReportPdfModal
                    reportId={selectedReportId}
                    onClose={() => setModalOpen(false)}
                />
            )}
        </div>
    );
}
