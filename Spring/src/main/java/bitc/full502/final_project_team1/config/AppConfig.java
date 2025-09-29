package bitc.full502.final_project_team1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://10.0.2.2:8080", "http://localhost:8080", "http://10.0.2.2")
                .allowedOrigins("*") // 데모용 전부 허용. 운영 시 도메인 지정 권장
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
                .allowCredentials(false);
    }

    /** 네이버 Static Map API 호출용 RestTemplate Bean 등록 */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}