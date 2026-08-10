package com.longvo.demo_identity_service.service;

import com.cloudinary.Cloudinary;
import com.longvo.demo_identity_service.dto.response.SignedUploadResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MediaService {

    private final Cloudinary cloudinary;

    public SignedUploadResponse signUpload(String folder, String publicId) {

        long timestamp = System.currentTimeMillis() / 1000;

        Map<String, Object> params = new HashMap<>();
        params.put("timestamp", timestamp);
        params.put("folder", folder);
        params.put("public_id", publicId);

        String signature = cloudinary.apiSignRequest(params,
                cloudinary.config.apiSecret);

        /*String publicUrl = String.format(
                "https://res.cloudinary.com/%s/%s/upload/%s/%s",
                cloudinary.config.cloudName,
                folder.contains("video") ? "video" : "image",
                folder,
                publicId
        );*/

        return new SignedUploadResponse(
                cloudinary.config.cloudName,
                cloudinary.config.apiKey,
                signature,
                timestamp,
                folder,
                publicId
        );
    }

}
