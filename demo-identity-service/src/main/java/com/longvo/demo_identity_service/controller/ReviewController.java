package com.longvo.demo_identity_service.controller;

import com.longvo.demo_identity_service.dto.request.ProductCreationRequest;
import com.longvo.demo_identity_service.dto.request.ProductUpdateRequest;
import com.longvo.demo_identity_service.dto.request.ReviewCreationRequest;
import com.longvo.demo_identity_service.dto.request.ReviewUpdateRequest;
import com.longvo.demo_identity_service.dto.response.*;
import com.longvo.demo_identity_service.entity.Product;
import com.longvo.demo_identity_service.service.ProductService;
import com.longvo.demo_identity_service.service.ReviewService;
import com.longvo.demo_identity_service.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReviewController {

    ReviewService reviewService;

    @GetMapping("/getReview")
    public ApiResponse<Page<ReviewResponse>> getReviewsByProductId(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "5") int rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponse> reviewPage = reviewService.getReviews(productId, rating, pageable);
        return ApiResponse.<Page<ReviewResponse>>builder()
                .result(reviewPage)
                .build();
    }

    @GetMapping("/getSummary")
    public ApiResponse<ReviewSummaryResponse> getReviewSummary(
            @RequestParam Long productId
    ) {

        ReviewSummaryResponse reviewPage = reviewService.getReviewSummary(productId);
        return ApiResponse.<ReviewSummaryResponse>builder()
                .result(reviewPage)
                .build();
    }

    @PostMapping
    public ApiResponse<List<ReviewResponse>> createReview(@RequestBody List<ReviewCreationRequest> request) {
        return ApiResponse.<List<ReviewResponse>>builder()
                .result(reviewService.createReview(request))
                .build();
    }
}


