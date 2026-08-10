package com.longvo.demo_identity_service.dto.response;

import com.longvo.demo_identity_service.entity.ProductCategory;
import com.longvo.demo_identity_service.entity.ReviewMedia;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewResponse {

    Long id;
    Integer rating;
    String content;
    LocalDateTime createdAt;
    String skuSnapshot; // copy từ OrderItem vi du Color: White + Size: XL
    String username;
    String avatarUrl;
    Boolean edited;
    LocalDateTime editedAt;
    Boolean hasMedias;

    List<ReviewMediaResponse> medias;

    public ReviewResponse(Long id, Integer rating, String content, LocalDateTime createdAt, String skuSnapshot, String username, String avatarUrl, Boolean edited, LocalDateTime editedAt, Boolean hasMedias) {
        this.id = id;
        this.rating = rating;
        this.content = content;
        this.createdAt = createdAt;
        this.skuSnapshot = skuSnapshot;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.edited = edited;
        this.editedAt = editedAt;
        this.hasMedias = hasMedias;
    }
}
