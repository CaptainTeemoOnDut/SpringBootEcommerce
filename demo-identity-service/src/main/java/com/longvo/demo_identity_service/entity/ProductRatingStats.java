package com.longvo.demo_identity_service.entity;

import com.longvo.demo_identity_service.enums.AddressType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "product_rating_stats",
        indexes = {
                @Index(name = "idx_avg_rating", columnList = "avgRating")
        }
)
public class ProductRatingStats {

    @Id
    private Long productId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    @Builder.Default
    private long totalRating = 0;

    @Builder.Default
    private long totalReview = 0;

    private BigDecimal avgRating;

    @Builder.Default
    private int fiveStarCount = 0;

    @Builder.Default
    private int fourStarCount = 0;

    @Builder.Default
    private int threeStarCount = 0;

    @Builder.Default
    private int twoStarCount = 0;

    @Builder.Default
    private int oneStarCount = 0;

}
