package com.longvo.demo_identity_service.entity;

import com.longvo.demo_identity_service.enums.ReviewStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "review",
        uniqueConstraints = {

                @UniqueConstraint(columnNames = {"user_id", "order_item_id"}) ////User không thể review cùng order_item 2 lần
        },
        indexes = {
                @Index(name = "idx_review_order_item", columnList = "order_item_id"),
                @Index(name = "idx_review_user", columnList = "user_id"),
                @Index(name = "idx_review_created", columnList = "created_at")
        }
)
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "rating")
    @Min(1)
    @Max(5)
    Integer rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    OrderItem orderItem;

    @Column(name = "content")
    String content;

    @Column(name = "created_at")
    @CreationTimestamp
    LocalDateTime createdAt;

    @Column(name = "edited_at", nullable = true)
    @UpdateTimestamp
    LocalDateTime editedAt;

    @Column(name = "is_edited")
    boolean isEdited = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "sku_snapshot")
    String skuSnapshot; // copy từ OrderItem vi du Color: White + Size: XL

    @Column(name = "has_medias")
    Boolean hasMedias = false;

    @Enumerated(EnumType.STRING)
    ReviewStatus status; // ACTIVE, HIDDEN, REPORTED

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<ReviewMedia> reviewMedia;
}
