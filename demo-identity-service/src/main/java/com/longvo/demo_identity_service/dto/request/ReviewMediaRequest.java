package com.longvo.demo_identity_service.dto.request;

import com.longvo.demo_identity_service.enums.ReviewMediaType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewMediaRequest {

    String publicId;

    String publicUrl;

    ReviewMediaType reviewMediaType;


}
