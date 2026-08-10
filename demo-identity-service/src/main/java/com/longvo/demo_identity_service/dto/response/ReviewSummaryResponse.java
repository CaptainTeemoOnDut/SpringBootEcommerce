package com.longvo.demo_identity_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewSummaryResponse {

    BigDecimal averageRating;   // 4.8
    Integer totalReviews;

    Integer fiveStarCount;
    Integer fourStarCount;
    Integer threeStarCount;
    Integer twoStarCount;
    Integer oneStarCount;

    //Map<Integer, Integer> ratingPercentages;



}
