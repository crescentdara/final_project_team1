import { useEffect, useRef } from "react";

function NaverMap({ latitude = 35.228, longitude = 128.889 }) {
    const mapRef = useRef(null);
    const mapInstance = useRef(null);
    const markerInstance = useRef(null);

    useEffect(() => {
        const { naver } = window;
        if (!naver || !mapRef.current) {
            console.error("⚠️ Naver 객체나 mapRef 없음");
            return;
        }

        console.log("🗺️ 지도 생성/갱신 실행됨:", latitude, longitude);

        // 지도 최초 생성
        if (!mapInstance.current) {
            mapInstance.current = new naver.maps.Map(mapRef.current, {
                center: new naver.maps.LatLng(latitude, longitude),
                zoom: 15,
            });

            markerInstance.current = new naver.maps.Marker({
                position: new naver.maps.LatLng(latitude, longitude),
                map: mapInstance.current,
            });
        } else {
            // 지도/마커 위치만 갱신
            const newPos = new naver.maps.LatLng(latitude, longitude);
            mapInstance.current.setCenter(newPos);
            markerInstance.current.setPosition(newPos);
        }
    }, [latitude, longitude]);

    return (
        <div
            ref={mapRef}
            style={{
                width: "100%",
                minHeight: "250px",   // ✅ 최소 높이 보장
                borderRadius: "8px",
                overflow: "hidden",   // ✅ 지도 깨짐 방지
            }}
        />
    );
}

export default NaverMap;
