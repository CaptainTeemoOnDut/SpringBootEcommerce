package com.longvo.demo_identity_service.entity;

import com.longvo.demo_identity_service.enums.ProductMediaStatus;
import com.longvo.demo_identity_service.enums.ProductMediaType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

@Entity
@Table(
        name = "product_variation_option_media",
        indexes = {
                //@Index(name = "idx_media_product", columnList = "product_id"),
                @Index(name = "idx_pvom_media_public_id", columnList = "public_id"),
                @Index(name = "idx_pvom_media_type_status", columnList = "type, status")
        }
)
public class ProductVariationOptionMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ProductMediaType type;

    @Column(nullable = false, unique = true)
    String publicId;

    @Column(nullable = false, columnDefinition = "TEXT")
    String publicUrl;

    @Column(nullable = false)
    Integer position = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ProductMediaStatus status = ProductMediaStatus.ACTIVE;

    @CreationTimestamp
    @Column(updatable = false)
    LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variation_option_id", nullable = false)
    ProductVariationOption productVariationOption;
}

