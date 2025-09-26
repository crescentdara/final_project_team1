/* global naver */

import {useEffect, useState} from "react";

export default function BuildingDetailModal({id, onClose}) {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);

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

    return (
        <div className="modal d-block" tabIndex="-1" style={{background:"rgba(0,0,0,0.5)"}}>
            <div className="modal-dialog modal-lg modal-dialog-scrollable">
                <div className="modal-content">
                    <div className="modal-header">
                        <h5 className="modal-title">건물 상세 정보</h5>
                        <button type="button" className="btn-close" onClick={onClose}></button>
                    </div>
                    <div className="modal-body">
                        {loading ? (
                            <p>로딩중…</p>
                        ) : !data ? (
                            <p className="text-danger">데이터를 불러오지 못했습니다.</p>
                        ) : (
                            <>
                                <table className="table table-sm">
                                    <tbody>
                                    <tr><th>ID</th><td>{data.id}</td></tr>
                                    <tr><th>지번주소</th><td>{data.lotAddress ?? "-"}</td></tr>
                                    <tr><th>건물명</th><td>{data.buildingName ?? "-"}</td></tr>
                                    <tr><th>주용도</th><td>{data.mainUseName ?? "-"}</td></tr>
                                    <tr><th>구조</th><td>{data.structureName ?? "-"}</td></tr>
                                    <tr><th>지상층수</th><td>{data.groundFloors ?? "-"}</td></tr>
                                    <tr><th>지하층수</th><td>{data.basementFloors ?? "-"}</td></tr>
                                    <tr><th>대지면적</th><td>{data.landArea ? `${data.landArea}㎡` : "-"}</td></tr>
                                    <tr><th>건축면적</th><td>{data.buildingArea ? `${data.buildingArea}㎡` : "-"}</td></tr>
                                    </tbody>
                                </table>

                                <hr />
                                <h6>위치</h6>
                                <div id={`map-${id}`} style={{width:"100%", height:"300px"}}></div>
                            </>
                        )}
                    </div>
                    <div className="modal-footer">
                        <button className="btn btn-secondary" onClick={onClose}>닫기</button>
                    </div>
                </div>
            </div>
        </div>
    );
}
