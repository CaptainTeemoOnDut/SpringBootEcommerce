package com.longvo.demo_identity_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.longvo.demo_identity_service.dto.response.UploadImageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ImageUploadService {

    Cloudinary cloudinary;

    public UploadImageResponse uploadFile(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String publicId = uploadResult.get("public_id").toString();
            String url = uploadResult.get("secure_url").toString();
            return new UploadImageResponse(publicId, url);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }
}
