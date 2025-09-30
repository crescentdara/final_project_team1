package bitc.full502.final_project_team1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 실제 저장 루트 디렉터리(물리 경로)
     * 예) /opt/app/upload  또는  C:/files/upload
     * FileStorageServiceImpl.storeFile(...) 와 동일한 프로퍼티를 사용한다.
     */
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * [변경점 ①] CORS 설정은 유지.
     * React 개발 서버(5173)에서 API/이미지에 접근 가능하도록 허용.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:5173")
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true);
    }

    /**
     * [변경점 ②] 정적 리소스 매핑 추가.
     *  - /upload/** 로 들어오는 요청을 uploadDir(물리 경로)에서 직접 서빙한다.
     *  - 리액트에선 DB에 저장된 "/upload/xxx/파일명"을 그대로 <img src>로 사용 가능.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // OS 절대경로 → "file:" URL로 변환 (마지막 슬래시 필수)
        String root = Paths.get(uploadDir).toAbsolutePath().normalize().toString()
            .replace('\\', '/');
        if (!root.endsWith("/")) root += "/";

        registry.addResourceHandler("/upload/**")
            .addResourceLocations("file:" + root)   // 예: file:/opt/app/upload/
            .setCachePeriod(60 * 60 * 24 * 365)     // (선택) 1년 캐시
            .resourceChain(true);                   // 리소스 체인 활성화
    }
}
