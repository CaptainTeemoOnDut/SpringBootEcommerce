package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.entity.Address;
import com.longvo.demo_identity_service.entity.ProductRatingStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRatingStatsRepository extends JpaRepository<ProductRatingStats, Long> {
    @Modifying
    @Query("""
        UPDATE ProductRatingStats s
        SET 
            s.totalRating = s.totalRating + :rating,
            s.totalReview = s.totalReview + 1,
            s.avgRating = (s.totalRating + :rating) * 1.0 / (s.totalReview + 1)
        WHERE s.productId = :productId
        """)
    void updateStatsAtomic(Long productId, int rating);
}
