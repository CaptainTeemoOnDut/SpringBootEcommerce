package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.dto.response.ReviewMediaResponse;
import com.longvo.demo_identity_service.dto.response.ReviewResponse;
import com.longvo.demo_identity_service.entity.Review;
import com.longvo.demo_identity_service.entity.ReviewMedia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewMediaRepository extends JpaRepository<ReviewMedia, Long> {

    @Query("""
    select new com.longvo.demo_identity_service.dto.response.ReviewMediaResponse(
        rm.id,
        rm.publicId,
        rm.publicUrl,
        rm.mediaType,
        rm.review.id,
        rm.createdAt
    )
    from ReviewMedia rm
    where rm.review.id in :reviewIds
    order by rm.createdAt asc
""")
    List<ReviewMediaResponse> findByReviewIds(
            @Param("reviewIds") List<Long> reviewIds
    );

}
