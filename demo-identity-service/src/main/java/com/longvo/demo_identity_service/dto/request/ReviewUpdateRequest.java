package com.longvo.demo_identity_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewUpdateRequest {
    Long id;
    int rating;
    String content;
    boolean isEdited = true;
    List<String> mediaPublicIdsToDelete;
}
