package com.longvo.demo_identity_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.longvo.demo_identity_service.dto.request.ReviewMediaRequest;
import com.longvo.demo_identity_service.dto.request.ReviewCreationRequest;
import com.longvo.demo_identity_service.dto.request.ReviewUpdateRequest;
import com.longvo.demo_identity_service.dto.response.*;
import com.longvo.demo_identity_service.entity.*;
import com.longvo.demo_identity_service.enums.ReviewStatus;
import com.longvo.demo_identity_service.exception.AppException;
import com.longvo.demo_identity_service.exception.ErrorCode;
import com.longvo.demo_identity_service.mapper.ReviewMapper;
import com.longvo.demo_identity_service.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReviewService {

    RatingService ratingService;
    ReviewRepository reviewRepository;
    ReviewMediaRepository reviewMediaRepository;
    UserRepository userRepository;
    VariantRepository variantRepository;
    OrderRepository orderRepository;
    ReviewMapper reviewMapper;
    ReviewMediaService reviewMediaService;
    OrderItemRepository orderItemRepository;
    ProductRepository productRepository;
    ProductRatingStatsRepository productRatingStatsRepository;
    private final ImageUploadService imageUploadService;
    private final Cloudinary cloudinary;

    public Page<ReviewResponse> getReviews(Long productId, Integer rating, Pageable pageable) {

        Page<ReviewResponse> page =
                reviewRepository.findReviewsByProduct(productId, rating, pageable);

        List<Long> reviewIds =
                page.getContent().stream()
                        .map(ReviewResponse::getId)
                        .toList();


        Map<Long, List<ReviewMediaResponse>> mediaMap =
                reviewMediaService.getByReviewIds(reviewIds);

        page.getContent().forEach(r ->
                r.setMedias(mediaMap.getOrDefault(r.getId(), List.of()))
        );

        return page;
    }

    /*public ReviewAggregateResponse getReviews(Long productId, Integer rating, Pageable pageable) {

        Page<ReviewResponse> page =
                reviewRepository.findReviewsByProduct(productId, rating, pageable);

        List<Long> reviewIds =
                page.getContent().stream()
                        .map(ReviewResponse::getId)
                        .toList();


        Map<Long, List<ReviewMediaResponse>> mediaMap =
                reviewMediaService.getByReviewIds(reviewIds);

        page.getContent().forEach(r ->
                r.setMedias(mediaMap.getOrDefault(r.getId(), List.of()))
        );

        return ReviewAggregateResponse.builder()
                .items(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }*/

    public ReviewSummaryResponse getReviewSummary(Long productId) {

        ProductRatingStats productRatingStats = productRatingStatsRepository.findById(productId)
                .orElseThrow(()-> new AppException(ErrorCode.PRODUCT_RATING_NOT_EXISTED));

        /*List<Object[]> rows =
                reviewRepository.countReviewsByRating(productId);

        Map<Integer, Long> ratingMap = new HashMap<>();

        long total = 0;
        long weightedSum = 0;

        for (Object[] row : rows) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];

            ratingMap.put(rating, count);

            total += count;
            weightedSum += rating * count;
        }

        BigDecimal average = total == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(weightedSum)
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);*/

        return ReviewSummaryResponse.builder()
                .averageRating(productRatingStats.getAvgRating())
                .totalReviews((int) productRatingStats.getTotalReview())
                .fiveStarCount(productRatingStats.getFiveStarCount())
                .fourStarCount(productRatingStats.getFourStarCount())
                .threeStarCount(productRatingStats.getThreeStarCount())
                .twoStarCount(productRatingStats.getTwoStarCount())
                .oneStarCount(productRatingStats.getOneStarCount())
                .build();
    }

    @Transactional
    public List<ReviewResponse> createReview(List<ReviewCreationRequest> Request) {

        List<ReviewResponse> responses = new ArrayList<>();

        for (ReviewCreationRequest reviewCreationRequest : Request) {
            Long userId = reviewCreationRequest.getUserId();

            List<ReviewMediaRequest> reviewMedias = new ArrayList<>() ;

            if (reviewCreationRequest.getProductImages() != null) {
                reviewMedias.addAll(reviewCreationRequest.getProductImages());

            }

            if (reviewCreationRequest.getProductVideo() != null) {
                reviewMedias.add(reviewCreationRequest.getProductVideo());
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            ProductVariant variant = variantRepository.findById(reviewCreationRequest.getVariantId())
                    .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_EXISTS));

            if (reviewRepository.existsByOrderItemId(reviewCreationRequest.getOrderItemId())) {
                throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
            }


            long reviewStart = System.currentTimeMillis();
            Review review = new Review();
            review.setRating(reviewCreationRequest.getProductRating());

            OrderItem orderItem = orderItemRepository.findById(reviewCreationRequest.getOrderItemId())
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_ITEM_NOT_EXISTED));

            review.setOrderItem(orderItem);
            review.setContent(reviewCreationRequest.getProductReviewDescription());

            if (!orderItem.getOrder().getUser().getId().equals(userId)) {
                throw new RuntimeException("Oder item does not belong to user");
            }

            if (!orderItem.getVariant().getId().equals(variant.getId())) {
                throw new RuntimeException("Variant mismatch with order item");
            }

            int newRating = reviewCreationRequest.getProductRating();
            if (newRating < 1 || newRating > 5) {
                throw new IllegalArgumentException("Invalid rating");
            }

            review.setUser(user);
            review.setEdited(false);
            review.setSkuSnapshot(orderItem.getSkuSnapshot());
            review.setHasMedias(!reviewMedias.isEmpty());
            review.setStatus(ReviewStatus.ACTIVE);
            reviewRepository.save(review);

            // update stats (atomic, không conflict, no race condition) (sync)
            Product product = orderItem.getVariant().getProduct();

            Long productId = product.getId();
            System.out.println("PRODUCT ID = " + productId);

            if (!productRatingStatsRepository.existsById(productId)) {
                ProductRatingStats productRatingStats = ProductRatingStats.builder()
                        .product(product)
                        .totalRating(newRating)
                        .totalReview(1)
                        .avgRating(BigDecimal.valueOf(newRating))
                        .fiveStarCount(0)
                        .fourStarCount(0)
                        .threeStarCount(0)
                        .twoStarCount(0)
                        .oneStarCount(0)
                        .build();
                productRatingStatsRepository.save(
                        productRatingStats
                );
            } else {
                productRatingStatsRepository.updateStatsAtomic(productId, newRating);
            }

            // 3. (optional) update product.avgRating (async)
            //ratingService.syncAvgRating(productId);

            log.info("✅ Save review done in {} ms", System.currentTimeMillis() - reviewStart);

            List<ReviewMedia> reviewMediaList = new ArrayList<>();
            if (reviewMedias != null) {
                long uploadStart = System.currentTimeMillis();
                for (ReviewMediaRequest req : reviewMedias) {
                    //UploadImageResponse uploadImageResponse = imageUploadService.uploadFile(file);

                    ReviewMedia media = new ReviewMedia();
                    media.setReview(review);
                    media.setPublicId(req.getPublicId());
                    media.setPublicUrl(req.getPublicUrl());
                    media.setMediaType(req.getReviewMediaType());

                    reviewMediaList.add(media);
                }
                reviewMediaRepository.saveAll(reviewMediaList);

            }


            ReviewResponse response = ReviewResponse.builder()
                    .id(review.getId())
                    .rating(review.getRating())
                    .content(review.getContent())
                    .createdAt(review.getCreatedAt())
                    .skuSnapshot(review.getSkuSnapshot())
                    .username(review.getUser().getUsername())
                    .avatarUrl(review.getUser().getAvatarUrl())
                    .edited(review.isEdited())
                    .editedAt(review.getEditedAt())
                    .hasMedias(review.getHasMedias())
                    .build();

            responses.add(response);
        }

        return responses;
    }

    /*@Transactional
    public ReviewResponse createReview(ReviewCreationRequest reviewCreationRequest) {

        long start = System.currentTimeMillis();  // Thời gian bắt đầu toàn bộ
        log.info("=== [START] createReview ===");

        long checkStart = System.currentTimeMillis();

        Long userId = reviewCreationRequest.getUserId();

        List<ReviewMediaRequest> reviewMedias = reviewCreationRequest.getImages();
        reviewMedias.add(reviewCreationRequest.getVideo());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        ProductVariant variant = variantRepository.findById(reviewCreationRequest.getVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_EXISTS));

        if (reviewRepository.existsByOrderItemId(reviewCreationRequest.getOrderItemId())) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
        log.info("✅ Check exists done in {} ms", System.currentTimeMillis() - checkStart);

        long reviewStart = System.currentTimeMillis();
        Review review = new Review();
        review.setRating(reviewCreationRequest.getRating());

        OrderItem orderItem = orderItemRepository.findById(reviewCreationRequest.getOrderItemId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_ITEM_NOT_EXISTED));

        review.setOrderItem(orderItem);
        review.setContent(reviewCreationRequest.getContent());

        if (!orderItem.getOrder().getUser().getId().equals(userId)) {
            throw new RuntimeException("Oder item does not belong to user");
        }

        if (!orderItem.getVariant().getId().equals(variant.getId())) {
            throw new RuntimeException("Variant mismatch with order item");
        }

        int newRating = reviewCreationRequest.getRating();
        if (newRating < 1 || newRating > 5) {
            throw new IllegalArgumentException("Invalid rating");
        }

        review.setUser(user);
        review.setEdited(false);
        review.setSkuSnapshot(orderItem.getSkuSnapshot());
        review.setHasMedias(!reviewMedias.isEmpty());
        review.setStatus(ReviewStatus.ACTIVE);
        reviewRepository.save(review);

        // update stats (atomic, không conflict, no race condition) (sync)
        Product product = orderItem.getVariant().getProduct();

        Long productId = product.getId();
        System.out.println("PRODUCT ID = " + productId);

        if (!productRatingStatsRepository.existsById(productId)) {
            ProductRatingStats productRatingStats = ProductRatingStats.builder()
                    .product(product)
                    .totalRating(newRating)
                    .totalReview(1)
                    .avgRating(BigDecimal.valueOf(newRating))
                    .fiveStarCount(0)
                    .fourStarCount(0)
                    .threeStarCount(0)
                    .twoStarCount(0)
                    .oneStarCount(0)
                    .build();
            productRatingStatsRepository.save(
                    productRatingStats
            );
        } else {
            productRatingStatsRepository.updateStatsAtomic(productId, newRating);
        }

        // 3. (optional) update product.avgRating (async)
        //ratingService.syncAvgRating(productId);

        log.info("✅ Save review done in {} ms", System.currentTimeMillis() - reviewStart);

        List<ReviewMedia> reviewMediaList = new ArrayList<>();
        if (reviewMedias != null) {
            long uploadStart = System.currentTimeMillis();
            for (ReviewMediaRequest req : reviewMedias) {
                //UploadImageResponse uploadImageResponse = imageUploadService.uploadFile(file);

                ReviewMedia media = new ReviewMedia();
                media.setReview(review);
                media.setPublicId(req.getPublicId());
                media.setPublicUrl(req.getPublicUrl());
                media.setMediaType(req.getReviewMediaType());

                reviewMediaList.add(media);
            }
            reviewMediaRepository.saveAll(reviewMediaList);
            log.info("✅ Upload and save media done in {} ms", System.currentTimeMillis() - uploadStart);
        }

        long end = System.currentTimeMillis();
        log.info("✅ [END] createReview total time: {} ms", end - start);

        return ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .skuSnapshot(review.getSkuSnapshot())
                .username(review.getUser().getUsername())
                .avatarUrl(review.getUser().getAvatarUrl())
                .edited(review.isEdited())
                .editedAt(review.getEditedAt())
                .hasMedias(review.getHasMedias())
                .build();
    }*/


    public ReviewResponse getReviewById(Long reviewId) {
        return reviewMapper.toReviewResponse(reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_EXISTED)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteReviewById(Long reviewId) {
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_EXISTED));

        reviewRepository.delete(review);
    }

    /*public ReviewResponse updateReview(Long reviewId, ReviewUpdateRequest request, MultipartFile[] newFiles) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new RuntimeException("Review not found");
        }

        Review review = new Review();
        reviewMapper.updateReview(review, request);

        // Xóa các anh ma user yeu cau
        if (request.getMediaPublicIdsToDelete() != null && !request.getMediaPublicIdsToDelete().isEmpty()) {

            List<ReviewMedia> mediasToDelete = reviewMediaRepository.findByPublicIdIn(request.getMediaPublicIdsToDelete());
            for (ReviewMedia media : mediasToDelete) {
                try {
                    cloudinary.uploader().destroy(media.getPublicId(), ObjectUtils.emptyMap());
                } catch (IOException e) {
                    // Ghi log lỗi nhưng không dừng
                    log.warn("Failed to delete media from Cloudinary: {}", media.getPublicId(), e);
                }
            }

            reviewMediaRepository.deleteAll(mediasToDelete);
        }

        // Upload ảnh mới nếu có
        if (newFiles != null) {
            for (MultipartFile file : newFiles) {
                try {
                    Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

                    ReviewMedia media = new ReviewMedia();
                    media.setReview(review);
                    media.setMediaUrl(uploadResult.get("secure_url").toString());
                    media.setPublicId(uploadResult.get("public_id").toString());
                    media.setMediaType(file.getContentType());

                    reviewMediaRepository.save(media);
                } catch (IOException e) {
                    log.error("Failed to upload media", e);
                    throw new RuntimeException("Upload failed");
                }
            }
        }

        reviewRepository.save(review);
        return reviewMapper.toReviewResponse(review);
    }*/
}
