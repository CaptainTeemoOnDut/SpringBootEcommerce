package com.longvo.demo_identity_service.service;

import com.longvo.demo_identity_service.dto.response.*;
import com.longvo.demo_identity_service.repository.ProductVariationRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@RequiredArgsConstructor

public class ProductVariationQueryService {

    private final ProductVariationRepository variationRepository;

    @Transactional(readOnly = true)
    public List<ProductVariationResponse> getProductVariations(Long productId) {

        List<ProductVariationFlatRow> rows =
                variationRepository.findFullVariations(productId);

        // Map variationId → ProductVariationResponse
        Map<Long, ProductVariationResponse> variationMap = new LinkedHashMap<>();

        for (ProductVariationFlatRow row : rows) {

            // 1. Get or create variation
            ProductVariationResponse variation =
                    variationMap.computeIfAbsent(
                            row.getVariationId(),
                            id -> {
                                ProductVariationResponse v = new ProductVariationResponse();
                                v.setId(id);
                                v.setCode(row.getVariationCode());
                                v.setName(row.getVariationName());
                                return v;
                            }
                    );

            // 2. Get or create option inside variation
            ProductVariationOptionResponse option =
                    variation.getOptions()
                            .stream()
                            .filter(o -> o.getId().equals(row.getOptionId()))
                            .findFirst()
                            .orElseGet(() -> {
                                ProductVariationOptionResponse o =
                                        new ProductVariationOptionResponse();
                                o.setId(row.getOptionId());
                                o.setValue(row.getOptionValue());
                                variation.getOptions().add(o);
                                return o;
                            });

            // 3. Add image (if exists)
            if (row.getImageUrl() != null) {
                option.getImages().add(row.getImageUrl());
            }
        }

        return new ArrayList<>(variationMap.values());
    }
}