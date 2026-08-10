package com.longvo.demo_identity_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryTreeResponse {
    Long id;
    String categoryName;
    //Set<Product> products;
    List<CategoryTreeResponse> children = new ArrayList<>();
}
