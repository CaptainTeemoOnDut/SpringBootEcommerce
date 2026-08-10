package com.longvo.demo_identity_service.mapper;

import com.longvo.demo_identity_service.dto.request.OrderHistoryRequest;
import com.longvo.demo_identity_service.dto.request.ReviewCreationRequest;
import com.longvo.demo_identity_service.dto.request.ReviewUpdateRequest;
import com.longvo.demo_identity_service.dto.response.OrderHistoryResponse;
import com.longvo.demo_identity_service.dto.response.ReviewResponse;
import com.longvo.demo_identity_service.entity.Order;
import com.longvo.demo_identity_service.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    Review toReview(ReviewCreationRequest request);

    ReviewResponse toReviewResponse(Review review);

    @Mapping(target = "id", ignore = true)
    void updateReview(@MappingTarget Review review, ReviewUpdateRequest request);
}
