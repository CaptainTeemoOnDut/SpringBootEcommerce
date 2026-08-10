package com.longvo.demo_identity_service.dto.response;

import com.longvo.demo_identity_service.enums.ReviewMediaType;
import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewMediaResponse {

    Long id;

    String publicId;

    String publicUrl;

    ReviewMediaType mediaType;

    Long reviewId;

    Date createdAt;

}
