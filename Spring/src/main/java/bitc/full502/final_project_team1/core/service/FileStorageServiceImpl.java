// src/main/java/bitc/full502/final_project_team1/core/service/FileStorageServiceImpl.java
package bitc.full502.final_project_team1.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    /**
     * 실제 저장 루트 디렉터리 (OS 절대경로)
     *
     * application.properties (또는 yml) 예시:
     *   file.upload-dir=/opt/app/upload       # Linux
     *   # file.upload-dir=C:/files/upload     # Windows
     */
    @Value("${file.upload-dir}")
    private String uploadDir;

    // 허용할 서브폴더 화이트리스트 (원하면 추가)
    private static final Set<String> ALLOWED_SUBFOLDERS = Set.of("ext", "ext-edit", "int", "int-edit");

    @Override
    public String storeFile(MultipartFile file, String subFolder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드 파일이 비어 있습니다.");
        }
        String sf = sanitizeSubFolder(subFolder);

        // 저장 루트/서브폴더 경로 정규화
        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path dirPath = root.resolve(sf).normalize();

        // 디렉터리 생성
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            throw new RuntimeException("업로드 디렉터리 생성 실패: " + dirPath, e);
        }

        // 새 파일명 (UUID + 확장자)
        String original = file.getOriginalFilename();
        String ext = safeExtension(original);
        String newFileName = UUID.randomUUID() + ext;

        // 최종 저장 경로
        Path target = dirPath.resolve(newFileName).normalize();

        // 경로 이탈 방지 (경로조작 차단)
        assertUnder(target, root);

        // 저장
        try {
            // Files.copy로 스트림 복사 (기존 파일 있으면 교체)
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + original, e);
        }

        // 프론트가 <img src>로 바로 쓸 상대 URL 반환
        // 결과: /upload/{subFolder}/{uuid}.ext
        return "/upload/" + (sf.isEmpty() ? "" : sf + "/") + newFileName;
    }

    /** subFolder 검증/정규화 */
    private String sanitizeSubFolder(String subFolder) {
        String sf = StringUtils.trimWhitespace(subFolder == null ? "" : subFolder);
        if (sf.isEmpty()) return "";
        if (!ALLOWED_SUBFOLDERS.contains(sf)) {
            throw new IllegalArgumentException("허용되지 않은 서브폴더: " + sf);
        }
        return sf;
    }

    /** 원본 파일명에서 안전하게 확장자 추출 (없으면 빈 문자열) */
    private String safeExtension(String filename) {
        if (!StringUtils.hasText(filename)) return "";
        String name = Paths.get(filename).getFileName().toString(); // 경로부 제거
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return "";
        String ext = name.substring(dot).toLowerCase(); // ".jpg"
        // 너무 긴/이상한 확장자 방지 (원하면 화이트리스트 적용)
        if (ext.length() > 10) return "";
        return ext;
    }

    /** 저장 대상 경로가 루트 디렉터리 하위인지 확인 (경로조작 방지) */
    private void assertUnder(Path target, Path root) {
        if (!target.toAbsolutePath().normalize().startsWith(root)) {
            throw new SecurityException("허용되지 않은 경로 접근");
        }
    }
}
