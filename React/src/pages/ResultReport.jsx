import React, { useEffect, useState } from "react";
import ReportPdfModal from "../components/modals/ReportPdfModal.jsx";
import Pagination from "../components/ui/Pagination.jsx";

/* =========================
   공통: 날짜 포맷 유틸
========================= */
const fmtDate = (v) => {
    if (v === null || v === undefined) return "-";

    let d;
    if (typeof v === "number") {
        // 초 단위(10자리) / 밀리초(13자리) 모두 대응
        d = new Date(v < 1e12 ? v * 1000 : v);
    } else {
        const s = String(v).trim();
        // "2025-10-01", "2025-10-01T12:34:56Z", "1706716800000" 등
        if (/^\d{10}$/.test(s)) d = new Date(Number(s) * 1000);
        else if (/^\d{13}$/.test(s)) d = new Date(Number(s));
        else d = new Date(s);
    }
    if (Number.isNaN(d.getTime())) return "-";

    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    return `${y}-${m}-${day}`;
};

/* createdAt 키 정규화: created_at(스네이크) 포함 다양한 케이스 흡수 */
const getCreatedAt = (r) =>
    r?.createdAt ??
    r?.created_at ??           // ✅ DB 컬럼명 스네이크 케이스
    r?.createdDate ??
    r?.created_date ??
    r?.createdOn ??
    r?.created_on ??
    r?.regDate ??
    r?.registeredAt ??
    null;


/* =========================
   상단 필터 (결재 대기중 UI과 톤 맞춤)
========================= */
function ReportFilters({ keyword, setKeyword, sort, setSort, onSearch, total }) {
    return (
        <>
            <div className="d-flex flex-wrap gap-2 align-items-center mb-2">
                <h3
                    className="fw-bold m-0 me-auto"
                    style={{ borderLeft: "4px solid #6898FF", paddingLeft: "12px" }}
                >
                    결재 완료 {" "}
                    <span className="ms-2 text-muted fs-6" style={{ fontSize: "0.9rem" }}>
                    (총 {total}개)
                </span>
                </h3>
            </div>
            <div className="d-flex gap-2 justify-content-end mb-3">
                <select
                    className="form-select"
                    style={{ maxWidth: 130 }}
                    value={sort}
                    onChange={(e) => setSort(e.target.value)}
                >
                    <option value="latest">최신 생성순</option>
                    <option value="oldest">오래된 순</option>
                </select>

                <div className="input-group" style={{ maxWidth: 320, height: 40  }}>
                    <input
                        className="form-control"
                        placeholder="관리번호 / 조사원 / 주소
                        검색"
                        value={keyword}
                        onChange={(e) => setKeyword(e.target.value)}
                        onKeyDown={(e) => e.key === "Enter" && onSearch()}
                    />
                    <button className="btn btn-outline-secondary" onClick={onSearch}>
                        검색
                    </button>
                </div>
            </div>
        </>
    );
}

/* =========================
   리스트 아이템 (결재 대기중 카드와 동일 톤)
========================= */
function ReportItem({ report, onOpen }) {
    return (
        <div
            className="report-item mb-3"
            onClick={() => onOpen(report.id)}
            role="button"
            aria-label="보고서 보기"
        >
            <div className="d-flex align-items-center justify-content-between">
                <div className="me-3">
                    <div className="fw-semibold">
                        {report.caseNo} · {report.investigator} · {report.address}
                    </div>
                </div>

                <div className="d-flex align-items-center gap-3 ms-auto">
                    <div className="text-muted small">
                        생성일 {fmtDate(getCreatedAt(report))}
                    </div>
                    <span
                        className="badge ri-pill"
                        onClick={(e) => {
                            e.stopPropagation();
                            onOpen(report.id);
                        }}
                    >
                        보고서
                    </span>
                </div>
            </div>
        </div>
    );
}


/* =========================
   페이지 컴포넌트
========================= */
export default function ResultReport() {
    const [reports, setReports] = useState([]);
    const [keyword, setKeyword] = useState("");
    const [sort, setSort] = useState("latest");

    // 페이지네이션
    const [page, setPage] = useState(1); // 1-based
    const [total, setTotal] = useState(0);
    const size = 7;

    // 모달
    const [modalOpen, setModalOpen] = useState(false);
    const [selectedReportId, setSelectedReportId] = useState(null);

    const fetchReports = () => {
        const params = new URLSearchParams({
            keyword: keyword,
            sort: sort,
            page: page - 1,
            size: size,
        });

        fetch(`/web/api/report?${params.toString()}`)
            .then((res) => {
                if (!res.ok) throw new Error("보고서 목록 불러오기 실패");
                return res.json();
            })
            .then((data) => {
                setReports(data.content || []);
                setTotal(data.totalElements || 0);
                console.log(data)
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

    // 결재대기중과 동일한 컨테이너 톤
    return (
        <>
            {/* 로컬 전용 스타일: 카드/배지/주소 너비 등 */}
            <style>{`
        .report-item {
          background: #fff;
          border: 1px solid #e9ecef;
          border-radius: 16px;
          padding: 14px 16px;
          box-shadow: 0 2px 6px rgba(0,0,0,0.06);
          transition: box-shadow .18s ease, transform .02s ease;
        }
        .report-item:hover {
          box-shadow: 0 6px 14px rgba(0,0,0,0.08);
        }
        .ri-address {
          max-width: 52vw; /* 한 줄 말줄임 폭 */
        }
        @media (max-width: 1400px) {
          .ri-address { max-width: 48vw; }
        }
        @media (max-width: 1200px) {
          .ri-address { max-width: 42vw; }
        }
        /* SENT 배지 톤과 유사한 파운더리 */
        .ri-pill {
          background-color: #f1f3f5;
          color: #495057;
          border-radius: 9999px;
          padding: 6px 12px;
          cursor: pointer;
          font-weight: 600;
          letter-spacing: .2px;
        }
      `}</style>

            <div
                className="container-fluid py-4"
                style={{
                    backgroundColor: "#fff",
                    borderRadius: "8px",
                    padding: "24px",
                    boxShadow: "0 2px 6px rgba(0,0,0,0.08)",
                    marginTop: "24px",
                    height: "100%"
                }}
            >
                {/* 필터 */}
                <ReportFilters
                    keyword={keyword}
                    setKeyword={setKeyword}
                    sort={sort}
                    setSort={setSort}
                    onSearch={() => {
                        setPage(1);
                        fetchReports();
                    }}
                    total={total}
                />

                {/* 리스트 */}
                {reports.length === 0 ? (
                    <div className="text-center text-muted py-5 border rounded-4 bg-light">
                        표시할 보고서가 없습니다.
                    </div>
                ) : (
                    reports.map((r) => <ReportItem key={r.id} report={r} onOpen={handleOpen} />)
                )}

                {/* 페이지네이션 (결재 대기중과 동일 위치/정렬) */}
                <Pagination
                    page={page}
                    total={total}
                    size={size}
                    onChange={setPage}
                    siblings={1}
                    boundaries={1}
                    className="justify-content-center mt-4"
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
        </>
    );
}
