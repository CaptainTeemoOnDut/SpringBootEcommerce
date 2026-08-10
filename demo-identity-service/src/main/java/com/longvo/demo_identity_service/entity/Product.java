package com.longvo.demo_identity_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.longvo.demo_identity_service.enums.ProductRejectReason;
import com.longvo.demo_identity_service.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "product", indexes = {
        // Index 1: Quan trọng nhất - Tối ưu lọc theo danh mục và trạng thái sản phẩm
        @Index(name = "idx_product_category_status", columnList = "category_id, product_status"),

        // Index 2: Tối ưu cho việc hiển thị danh sách sản phẩm mới nhất/cũ nhất
        @Index(name = "idx_product_created_at", columnList = "createdAt"),

        // Index 3: Tối ưu nếu bạn thường xuyên lọc sản phẩm theo Shop
        @Index(name = "idx_product_shop", columnList = "shop_id")
})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true,nullable = false, updatable = false)
    String sku;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String thumbnailUrl;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(nullable = false)
    BigDecimal unitPrice;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductMedia> medias = new ArrayList<>();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "product_status", nullable = false)
    ProductStatus status = ProductStatus.DRAFT;

    LocalDateTime approvedAt;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    User approvedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "reject_reason")
    ProductRejectReason rejectReason;

    @Column(name = "reject_note")
    String rejectNote;

    @Builder.Default
    @Column(nullable = false)
    Integer unitsInStock = 0;

    @Builder.Default
    @Column(nullable = false)
    Integer soldCount = 0;


    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @JsonIgnore
    Shop shop;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    ProductCategory category;

    Long weight;
    Long width;
    Long length;
    Long height;

    @Column(name = "product_condition")
    String condition;

    @Column(name = "avg_rating")
    private Double avgRating; //mỗi lần có review mới update lại

    //@Column(name = "total_review")
    //private int totalReview; //mỗi lần có review mới update lại

    public void addMedia(ProductMedia media) {
        if (media == null) return;

        media.setProduct(this);
        this.medias.add(media);
    }

    public void removeMedia(ProductMedia media) {
        if (media == null) return;

        media.setProduct(null);
        this.medias.remove(media);
    }


}
// ./mvnw clean compile
