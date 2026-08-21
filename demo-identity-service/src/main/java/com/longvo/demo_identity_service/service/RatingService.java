package com.longvo.demo_identity_service.service;

import com.longvo.demo_identity_service.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RatingService {
    ProductRepository productRepository;

    @Async("taskExecutor")
    public void syncAvgRating(Long productId) {
        try {
            productRepository.updateAvgRating(productId);
        } catch (Exception e) {
            log.error("Failed to sync avg rating", e);
        }
    }
}
