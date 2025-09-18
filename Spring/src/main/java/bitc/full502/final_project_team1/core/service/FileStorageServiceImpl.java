package bitc.full502.final_project_team1.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public String storeFile(MultipartFile file, String subFolder) {
        try {
            // 서브폴더 생성 (예: ext, int, ext-edit, int-edit)
            Path dirPath = Paths.get(uploadDir, subFolder).toAbsolutePath().normalize();
            Files.createDirectories(dirPath);

            // 파일명 생성 (UUID + 확장자)
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = UUID.randomUUID() + ext;

            // 실제 저장
            Path target = dirPath.resolve(newFileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            // DB에는 상대 경로만 저장 (/upload/... 형태)
            return "/upload/" + subFolder + "/" + newFileName;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + file.getOriginalFilename(), e);
        }
    }
}
