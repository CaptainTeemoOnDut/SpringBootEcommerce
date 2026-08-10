package com.longvo.demo_identity_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.longvo.demo_identity_service.entity.Product;
import com.longvo.demo_identity_service.entity.ProductVariationOption;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductVariationResponse {


    private Long id;


    private String code; // color, size


    private String name; // Mau sac, Kich thuoc


    private List<ProductVariationOptionResponse> options = new ArrayList<>();


    public ProductVariationResponse(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
