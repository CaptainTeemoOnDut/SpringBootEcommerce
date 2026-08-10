package com.longvo.demo_identity_service.dto.response;

import com.longvo.demo_identity_service.entity.ReviewMedia;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UploadImageResponse {

    String url;
    String publicId;
}
