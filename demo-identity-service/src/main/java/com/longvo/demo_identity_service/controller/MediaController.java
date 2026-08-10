package com.longvo.demo_identity_service.controller;

import com.longvo.demo_identity_service.dto.request.PresignRequest;
import com.longvo.demo_identity_service.dto.request.UserCreationRequest;
import com.longvo.demo_identity_service.dto.request.UserUpdateRequest;
import com.longvo.demo_identity_service.dto.response.ApiResponse;
import com.longvo.demo_identity_service.dto.response.SignedUploadResponse;
import com.longvo.demo_identity_service.dto.response.UserResponse;
import com.longvo.demo_identity_service.repository.UserRepository;
import com.longvo.demo_identity_service.service.MediaService;
import com.longvo.demo_identity_service.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MediaController {
    private final MediaService mediaService;

    @PostMapping("/presign")
    public SignedUploadResponse presign(@RequestBody PresignRequest req) {

        // validate ở BE
        if (req.getType().equals("VIDEO") && req.getSize() > 16 * 1024 * 1024) {
            throw new RuntimeException("Video quá lớn");
        }

        String folder = req.getType().equals("VIDEO")
                ? "shopee_clone/videos"
                : "shopee_clone/images";

        String publicId = UUID.randomUUID().toString();

        return mediaService.signUpload(folder, publicId);
    }
}
