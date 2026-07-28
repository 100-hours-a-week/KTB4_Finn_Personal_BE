package kr.ktb.finn_week6.global.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageStorageService {
    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024;
    private static final Set<String> ALLOWED_DOMAINS = Set.of("users", "posts");
    private static final Map<String, String> EXTENSIONS_BY_CONTENT_TYPE = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final Path uploadDir;

    public ImageStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file, String domain) {
        validateDomain(domain);
        validateFile(file);

        String extension = EXTENSIONS_BY_CONTENT_TYPE.get(file.getContentType());
        String fileName = UUID.randomUUID() + extension;
        Path domainDir = uploadDir.resolve(domain).normalize();
        Path targetPath = domainDir.resolve(fileName).normalize();

        try {
            Files.createDirectories(domainDir);
            Files.copy(file.getInputStream(), targetPath);
        } catch (IOException e) {
            throw new IllegalStateException("이미지 저장에 실패했습니다.", e);
        }

        return "/images/" + domain + "/" + fileName;
    }

    private void validateDomain(String domain) {
        if (!ALLOWED_DOMAINS.contains(domain)) {
            throw new IllegalArgumentException("지원하지 않는 이미지 저장 위치입니다.");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 비어있습니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("이미지 파일은 5MB 이하만 업로드할 수 있습니다.");
        }

        if (!EXTENSIONS_BY_CONTENT_TYPE.containsKey(file.getContentType())) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다.");
        }
    }
}
