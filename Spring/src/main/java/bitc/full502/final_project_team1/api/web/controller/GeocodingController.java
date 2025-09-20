package bitc.full502.final_project_team1.api.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RestController
@RequestMapping("/web/api/geocode")
@RequiredArgsConstructor
public class GeocodingController {

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    // ✅ 주소 전처리 메서드
    private String normalizeAddress(String query) {
        if (query == null) return "";

        String normalized = query.trim();

        // 1. 숫자 앞자리 0 제거 (예: 0109 → 109)
        normalized = normalized.replaceAll("\\b0+(\\d+)", "$1");

        // 2. -0001 같은 패턴 정리 (예: 109-0001 → 109-1)
        normalized = normalized.replaceAll("-(0+)(\\d+)", "-$2");

        // 3. "로"만 있고 번지 없는 경우 → "로 9번지" 형태 보정
        if (normalized.matches(".*로 \\d+$")) {
            normalized = normalized + "번지";
        }

        return normalized;
    }

    // ✅ 좌표 조회 API
    @GetMapping
    public ResponseEntity<?> getCoordinates(@RequestParam String query) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Query parameter is empty"));
        }

        try {
            String normalized = normalizeAddress(query);

            String url = UriComponentsBuilder
                    .fromHttpUrl("https://maps.apigw.ntruss.com/map-geocode/v2/geocode")
                    .queryParam("query", normalized)
                    .encode()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
            headers.set("X-NCP-APIGW-API-KEY", clientSecret);
            headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response =
                    restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "error", "Geocoding failed",
                    "message", e.getMessage()
            ));
        }
    }
}
