package kr.ktb.finn_week6.global.storage;

import kr.ktb.finn_week6.global.RequestMessage;
import kr.ktb.finn_week6.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageStorageService imageStorageService;

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ImageUploadResponse> uploadUserImage(@RequestParam("file") MultipartFile file) {
        return upload(file, "users");
    }

    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ImageUploadResponse> uploadPostImage(@RequestParam("file") MultipartFile file) {
        return upload(file, "posts");
    }

    private ApiResponse<ImageUploadResponse> upload(MultipartFile file, String domain) {
        String imagePath = imageStorageService.store(file, domain);
        String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(imagePath)
                .toUriString();

        return new ApiResponse<>(RequestMessage.SUCCESS.getDescription(), new ImageUploadResponse(imageUrl));
    }
}
