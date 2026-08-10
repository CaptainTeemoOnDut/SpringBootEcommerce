package com.longvo.demo_identity_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewCreationRequest {

    Long orderItemId;

    int productRating;

    String productReviewDescription;

    List<ReviewMediaRequest> productImages;

    ReviewMediaRequest productVideo;

    Long userId;

    int sellerRating;

    int deliveryRating;

    Long variantId;

    List<String> removeImages;

    String removeVideo;


}
