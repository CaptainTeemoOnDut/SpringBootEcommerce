package com.longvo.demo_identity_service.controller;

import com.longvo.demo_identity_service.dto.response.ApiResponse;
import com.longvo.demo_identity_service.dto.response.CategoryHomeResponse;
import com.longvo.demo_identity_service.dto.response.CategoryTreeResponse;
import com.longvo.demo_identity_service.entity.interfaces.CategoryAttributeProjection;
import com.longvo.demo_identity_service.service.ProductCategoryAttributeService;
import com.longvo.demo_identity_service.service.ProductCategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product-categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductCategoryController {
    ProductCategoryService productCategoryService;
    ProductCategoryAttributeService productCategoryAttributeService;

    @GetMapping("/seller")
    ApiResponse<List<CategoryTreeResponse>> getCategoryTreeForSeller() {

        return ApiResponse.<List<CategoryTreeResponse>>builder()
                .result(productCategoryService.getProductCategoryTree())
                .build();
    }

    @GetMapping("/{categoryId}/attributes")
    public ApiResponse<List<CategoryAttributeProjection>> getCategoryAttributes(
            @PathVariable Long categoryId) {

        return ApiResponse.<List<CategoryAttributeProjection>>builder()
                .result(productCategoryAttributeService.getAttributesForCategory(categoryId))
                .build();
    }

    @GetMapping("/home")
    ApiResponse<List<CategoryHomeResponse>> getCategoriesForHome() {

        return ApiResponse.<List<CategoryHomeResponse>>builder()
                .result(productCategoryService.getCategoriesForHome())
                .build();
    }
}
