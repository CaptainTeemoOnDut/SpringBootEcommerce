package com.longvo.demo_identity_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.longvo.demo_identity_service.entity.ProductVariation;
import com.longvo.demo_identity_service.entity.ProductVariationOptionMedia;
import com.longvo.demo_identity_service.repository.ProductVariationRepository;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductVariationOptionResponse {

    private Long id;

    private String value; // Black, XL

    private List<String> images = new ArrayList<>();

}





