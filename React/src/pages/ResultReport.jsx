import { useEffect, useState } from "react";
import axios from "axios";

function ResultReport() {
    const [reports, setReports] = useState([]);

    // ✅ 컴포넌트 mount 시 보고서 목록 불러오기
    useEffect(() => {
        axios
            .get("/web/report")
            .then((res) => {
                console.log("✅ 보고서 응답:", res.data); // 구조 확인용
                // 응답이 배열인지 확인 후 세팅
                if (Array.isArray(res.data)) {
                    setReports(res.data);
                } else if (res.data.content) {
                    // JPA Page 응답일 경우
                    setReports(res.data.content);
                } else {
                    // 단일 객체일 경우
                    setReports([res.data]);
                }
            })
            .catch((err) => console.error("❌ 보고서 목록 불러오기 실패:", err));
    }, []);

    return (
        <div className="container mt-4">
            <h2>결과 보고서 목록</h2>
            <table className="table table-striped mt-3">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>조사원</th>
                    <th>건물 주소</th>
                    <th>결재자</th>
                    <th>결재일시</th>
                </tr>
                </thead>
                <tbody>
                {reports.length > 0 ? (
                    reports.map((r) => (
                        <tr key={r.assignmentId}>
                            <td>{r.assignmentId}</td>
                            <td>{r.userName}</td>
                            <td>{r.buildingAddress}</td>
                            <td>{r.approvedByName}</td>
                            <td>{r.approvedAt}</td>
                        </tr>
                    ))
                ) : (
                    <tr>
                        <td colSpan="5" className="text-center">
                            결과 보고서가 없습니다.
                        </td>
                    </tr>
                )}
                </tbody>

            </table>
        </div>
    );
}

export default ResultReport;
