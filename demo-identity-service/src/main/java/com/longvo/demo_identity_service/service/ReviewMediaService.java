package com.longvo.demo_identity_service.service;

import com.cloudinary.Cloudinary;
import com.longvo.demo_identity_service.dto.response.ReviewAggregateResponse;
import com.longvo.demo_identity_service.dto.response.ReviewMediaResponse;
import com.longvo.demo_identity_service.dto.response.ReviewResponse;
import com.longvo.demo_identity_service.dto.response.VariantOptionValueResponse;
import com.longvo.demo_identity_service.mapper.ReviewMapper;
import com.longvo.demo_identity_service.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReviewMediaService {

    ReviewRepository reviewRepository;
    ReviewMediaRepository reviewMediaRepository;
    UserRepository userRepository;
    //VariantRepository variantRepository;
    OrderRepository orderRepository;
    ReviewMapper reviewMapper;
    private final ImageUploadService imageUploadService;
    private final Cloudinary cloudinary;

    public Map<Long, List<ReviewMediaResponse>> getByReviewIds(List<Long> reviewIds) {

        if (reviewIds == null || reviewIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<ReviewMediaResponse> medias =
                reviewMediaRepository.findByReviewIds(reviewIds);

        return medias.stream()
                .collect(Collectors.groupingBy(
                        ReviewMediaResponse::getReviewId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }


    /*public ReviewResponse createReview(ReviewCreationRequest reviewCreationRequest, MultipartFile[] files) {

        long start = System.currentTimeMillis();  // Thời gian bắt đầu toàn bộ
        log.info("=== [START] createReview ===");

        long checkStart = System.currentTimeMillis();
        if (!userRepository.existsById(reviewCreationRequest.getUserId())) {
            throw new RuntimeException("User not found");
        }

        if (!variantRepository.existsById(reviewCreationRequest.getVariantId())) {
            throw new RuntimeException("Variant not found");
        }

        if (!orderRepository.existsById(reviewCreationRequest.getOrderId())) {
            throw new RuntimeException("Order not found");
        }

        if (reviewRepository.existsByOrderId(reviewCreationRequest.getOrderId())) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
        log.info("✅ Check exists done in {} ms", System.currentTimeMillis() - checkStart);

        long reviewStart = System.currentTimeMillis();
        Review review = new Review();
        review.setRating(reviewCreationRequest.getRating());
        review.setContent(reviewCreationRequest.getContent());

        review.setUser(userRepository.getReferenceById(reviewCreationRequest.getUserId())); //proxy
        review.setVariant(variantRepository.getReferenceById(reviewCreationRequest.getVariantId())); //proxy
        review.setOrder(orderRepository.getReferenceById(reviewCreationRequest.getOrderId())); //proxy

        reviewRepository.save(review);
        log.info("✅ Save review done in {} ms", System.currentTimeMillis() - reviewStart);

        if (files != null) {
            long uploadStart = System.currentTimeMillis();
            for (MultipartFile file : files) {
                UploadImageResponse uploadImageResponse = imageUploadService.uploadFile(file);

                ReviewMedia media = new ReviewMedia();
                media.setReview(review);
                media.setPublicId(uploadImageResponse.getPublicId());
                media.setMediaUrl(uploadImageResponse.getUrl());
                media.setMediaType(file.getContentType());

                reviewMediaRepository.save(media);
            }
            log.info("✅ Upload and save media done in {} ms", System.currentTimeMillis() - uploadStart);
        }

        long end = System.currentTimeMillis();
        log.info("✅ [END] createReview total time: {} ms", end - start);

        return reviewMapper.toReviewResponse(review);
    }

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

    public ReviewResponse updateReview(Long reviewId, ReviewUpdateRequest request, MultipartFile[] newFiles) {
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
