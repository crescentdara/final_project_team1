package bitc.full502.final_project_team1.core.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * 파일을 서버 디스크에 저장하고, 클라이언트가 <img src>에 바로 쓸 수 있는
     * 상대 URL("/upload/{subFolder}/{filename}")을 반환한다.
     *
     * @param file      업로드 파일 (null/empty 허용하지 않음)
     * @param subFolder 하위 폴더명 (예: "ext", "ext-edit", "int", "int-edit")
     * @return          저장된 파일의 공개 상대 URL
     */
    String storeFile(MultipartFile file, String subFolder);
}
