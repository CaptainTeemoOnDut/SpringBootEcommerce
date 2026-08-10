package com.longvo.demo_identity_service.controller;

import com.longvo.demo_identity_service.dto.response.ApiResponse;
import com.longvo.demo_identity_service.dto.response.UploadImageResponse;
import com.longvo.demo_identity_service.service.ImageUploadService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ImageController {
    ImageUploadService imageUploadService;

    @PostMapping("/upload")
    public ApiResponse<UploadImageResponse> uploadImage(@RequestParam("file") MultipartFile file) {

        return ApiResponse.<UploadImageResponse>builder()
                .result(imageUploadService.uploadFile(file))
                .build();
    }
}
