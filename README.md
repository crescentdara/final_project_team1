# 김해시 건축물 현황 실태조사 (Web & Android)

조사원 모바일 앱(Android), 관리/승인 웹(React), Spring Boot 백엔드 + MySQL이 연동된 김해시 건축물 현황 실태조사 통합 플랫폼입니다.
GPS/지도, AR 오버레이, 사진 촬영·편집, 조사결과 임시저장 및 전송, 업무배정–조사–승인(반려) 워크플로우, 재조사 관리를 제공합니다.

## 기능
- **Android(조사원)**: 배정목록 조회, 지도/반경거리 확인, 길찾기 TMAP 연동, 카메라 AR 오버레이, 사진 촬영·편집, 임시저장(TEMP)→전송(SENT), 재조사(반려사유 확인), 조사내역 조회(결재대기 중, 결재완료), 통계, 메세지 수신
- **Web(관리자/결재자)**: 대시보드(통계), 조사결과 승인/반려(사유 기록), 조사지 배정, 결재자 배정, 메시지 발신, 조사원 생성/수정/삭제, 조사지 단건/다건(엑셀) 등록, 지도, 결과보고서 PDF 다운로드, 조사목록 전체 조회
- **Backend(API)**: JPA 엔티티(건물/승인/배정/메세지/조사결과/보고서/유저), 파일 업로드, 레포트 저장

## 기술 스택
- **Backend**: Java 17, Spring Boot 3.5.5, JPA(Hibernate), MySQL8, HikariCP
- **Web**: React 19, styled-components, React Router, Axios
- **Mobile**: Kotlin, Jetpack, Naver Map SDK, Sensor/Location, FileProvider
