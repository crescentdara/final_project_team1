/* global naver */
import {useEffect, useState} from "react";

export default function BuildingDetailPanel({id, onClose, onEdit, onDelete, isApproved, deleting}) {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [visible, setVisible] = useState(true); // ✅ 애니메이션용 상태

    // 지도 표시
    useEffect(() => {
        if (data?.latitude && data?.longitude && window.naver) {
            const mapId = `map-${id}`;
            const map = new naver.maps.Map(mapId, {
                center: new naver.maps.LatLng(data.latitude, data.longitude),
                zoom: 16,
            });
            new naver.maps.Marker({
                position: new naver.maps.LatLng(data.latitude, data.longitude),
                map,
            });
        }
    }, [data, id]);

    // 상세 데이터 로드
    useEffect(() => {
        (async () => {
            try {
                const r = await fetch(`/web/building/${id}`);
                if (!r.ok) throw new Error("조회 실패");
                setData(await r.json());
            } catch (e) {
                console.error(e);
                setData(null);
            } finally {
                setLoading(false);
            }
        })();
    }, [id]);

    // 닫기 버튼 → 애니메이션 후 언마운트
    const handleClose = () => {
        setVisible(false);
        setTimeout(onClose, 300); // transition 시간과 동일
    };

    return (
        <div
            className="detail-panel"
            style={{
                width: visible ? "40%" : "0",
                minWidth: visible ? "400px" : "0",
                background: "#fff",
                boxShadow: "-2px 0 8px rgba(0,0,0,0.05)",
                borderLeft: "1px solid #ddd",
                padding: visible ? "20px" : "0",
                overflowY: "auto",
                transition: "all 0.3s ease",
                borderRadius: "12px 12px",
            }}
        >
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h5 className="m-0">건물 상세 정보</h5>
                <button type="button" className="btn-close" onClick={handleClose}></button>
            </div>

            {loading ? (
                <p>로딩중…</p>
            ) : !data ? (
                <p className="text-danger">데이터를 불러오지 못했습니다.</p>
            ) : (
                <>
                    {/*<h6>위치</h6>*/}
                    <div id={`map-${id}`} style={{width: "100%", height: "300px"}}></div>
                    <hr/>

                    <table className="table table-sm">
                        <tbody>
                        <tr>
                            <th>ID</th>
                            <td>{data.id}</td>
                        </tr>
                        <tr>
                            <th>지번주소</th>
                            <td>{data.lotAddress ?? "-"}</td>
                        </tr>
                        <tr>
                            <th>건물명</th>
                            <td>{data.buildingName ?? "-"}</td>
                        </tr>
                        <tr>
                            <th>주용도</th>
                            <td>{data.mainUseName ?? "-"}</td>
                        </tr>
                        <tr>
                            <th>구조</th>
                            <td>{data.structureName ?? "-"}</td>
                        </tr>
                        <tr>
                            <th>지상층수</th>
                            <td>{data.groundFloors ?? "-"}</td>
                        </tr>
                        <tr>
                            <th>지하층수</th>
                            <td>{data.basementFloors ?? "-"}</td>
                        </tr>
                        <tr>
                            <th>대지면적</th>
                            <td>{data.landArea ? `${data.landArea}㎡` : "-"}</td>
                        </tr>
                        <tr>
                            <th>건축면적</th>
                            <td>{data.buildingArea ? `${data.buildingArea}㎡` : "-"}</td>
                        </tr>
                        </tbody>
                    </table>
                    {/* ✅ 우측 하단 고정 버튼 바 */}
                    <div
                        className="position-sticky"
                        style={{
                            bottom: 0,
                            background: "#fff",
                            borderTop: "1px solid #e5e7eb",
                            paddingTop: 12,
                            paddingBottom: 12,
                            marginTop: 12,
                            zIndex: 2
                        }}
                    >
                        <div className="d-flex justify-content-end gap-2">
                            <button
                                type="button"
                                className="btn btn-outline-secondary"
                                onClick={onEdit}
                            >
                                수정
                            </button>
                            <button
                                type="button"
                                className="btn btn-danger"
                                onClick={onDelete}
                                disabled={deleting || isApproved}
                                title={isApproved ? "승인(결재 완료) 상태는 삭제할 수 없습니다." : ""}
                            >
                                {deleting ? "삭제 중…" : "삭제"}
                            </button>
                        </div>
                    </div>

                </>
            )}
        </div>
    );
}
