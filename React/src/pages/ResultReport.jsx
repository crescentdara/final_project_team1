import { useEffect, useState } from "react";
import axios from "axios";

function ResultReport() {
    const [reports, setReports] = useState([]);

    // ✅ 전체 보고서 불러오기
    useEffect(() => {
        axios.get("/web/api/report")
            .then((res) => setReports(res.data))
            .catch((err) => console.error("보고서 목록 불러오기 실패:", err));
    }, []);

    // ✅ PDF 보기 (새 창 열기)
    const handleViewPdf = (reportId) => {
        window.open(`/web/api/report/pdf/${reportId}`, "_blank");
    };

    // ✅ PDF 다운로드
    const handleDownloadPdf = (reportId) => {
        axios({
            url: `/web/api/report/pdf/${reportId}`,
            method: "GET",
            responseType: "blob", // 중요
        }).then((res) => {
            const url = window.URL.createObjectURL(new Blob([res.data]));
            const link = document.createElement("a");
            link.href = url;
            link.setAttribute("download", `report-${reportId}.pdf`);
            document.body.appendChild(link);
            link.click();
        });
    };

    return (
        <div className="container">
            <h2>📑 결과 보고서</h2>
            <table border="1" width="100%">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>조사자</th>
                    <th>주소</th>
                    <th>승인자</th>
                    <th>승인일시</th>
                    <th>PDF</th>
                </tr>
                </thead>
                <tbody>
                {reports.map((r) => (
                    <tr key={r.id}>
                        <td>{r.id}</td>
                        <td>{r.surveyResult?.user?.name ?? "-"}</td>
                        <td>{r.surveyResult?.building?.lotAddress ?? "-"}</td>
                        <td>{r.approvedBy?.name ?? "-"}</td>
                        <td>{r.approvedAt}</td>
                        <td>
                            <button onClick={() => handleViewPdf(r.id)}>보기</button>
                            <button onClick={() => handleDownloadPdf(r.id)}>다운로드</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

export default ResultReport;
