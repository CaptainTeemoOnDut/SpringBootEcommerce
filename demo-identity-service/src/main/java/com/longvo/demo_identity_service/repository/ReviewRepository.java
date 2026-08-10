package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.dto.response.ReviewResponse;
import com.longvo.demo_identity_service.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    //boolean existsByOrderId(Long orderId);

    @Query(
            value = """
        select new com.longvo.demo_identity_service.dto.response.ReviewResponse(
            r.id,
            r.rating,
            r.content,
            r.createdAt,
            oi.skuSnapshot,
            u.username,
            u.avatarUrl,
            r.isEdited,
            r.editedAt,
            r.hasMedias)
        
        from Review r
        join r.user u
        join r.orderItem oi
        where oi.variant.product.id = :productId
          and (:rating is null or r.rating = :rating)
        order by r.createdAt desc
    """,
            countQuery = """
        select count(r.id)
        from Review r
        join r.orderItem oi
        where oi.variant.product.id = :productId
          and (:rating is null or r.rating = :rating)
    """
    )
    Page<ReviewResponse> findReviewsByProduct(
            @Param("productId") Long productId,
            @Param("rating") Integer rating,
            Pageable pageable
    );

    @Query("""
    select r.rating, count(r.id)
    from Review r
    join r.orderItem oi
    where oi.variant.product.id = :productId
    group by r.rating
""")
    List<Object[]> countReviewsByRating(
            @Param("productId") Long productId
    );


    boolean existsByOrderItemId(Long orderItemId);
}
