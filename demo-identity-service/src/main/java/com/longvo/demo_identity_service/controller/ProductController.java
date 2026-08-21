package com.longvo.demo_identity_service.controller;

import com.longvo.demo_identity_service.dto.request.*;
import com.longvo.demo_identity_service.dto.response.*;
import com.longvo.demo_identity_service.service.ProductService;
import com.longvo.demo_identity_service.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {

    ProductService productService;
    ProductVariantService productVariantService;

    // filter database bằng querydsl
    @GetMapping
    public ApiResponse<PagedResponse<ProductCardResponse>> getProducts(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minRating,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,

            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {

        ProductFilterRequest request = new ProductFilterRequest();
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);
        request.setCategoryId(categoryId);
        request.setMinRating(minRating);

        Page<ProductCardResponse> productCardPage = productService.filterProductCards(
                request, page, size, sortBy, direction
        );
        return buildPagedApiResponse(productCardPage);

    }

    // full-text search / custom search query
    @GetMapping("/search")
    public ApiResponse<PagedResponse<ProductCardResponse>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double ratingFilter,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String order,
            Pageable pageable
    ) {

        ProductSearchRequest request = new ProductSearchRequest();
        request.setKeyword(keyword);
        request.setCategoryId(categoryId);
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);
        request.setMinRating(ratingFilter);
        request.setSort(sortBy);
        request.setOrder(order);

        Page<ProductCardResponse> productCardPage = productService.search(request, pageable);

        return buildPagedApiResponse(productCardPage);
    }

    @GetMapping("/category")
    public ApiResponse<PagedResponse<ProductResponse>> getProductsByCategoryId(
            @RequestParam Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> productPage = productService.getProductsByCategoryId(id, pageable);
        return buildPagedApiResponse(productPage);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDetailResponse> getProductDetailById(
            @PathVariable Long id

    ) {
        return ApiResponse.<ProductDetailResponse>builder()
                .result(productService.getProductDetail(id))
                .build();
    }

    @GetMapping("/variants/{variantId}/revalidate")
    public ApiResponse<VariantInfoResponse> revalidate(
            @PathVariable Long variantId

    ) {
        return ApiResponse.<VariantInfoResponse>builder()
                .result(productVariantService.revalidate(variantId))
                .build();
    }

    @GetMapping("/shop/{shopId}")
    public ApiResponse<PagedResponse<ProductResponse>> getProductsByShopId(
            @PathVariable("shopId") String shopId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<ProductResponse> productPage = productService.getProductsByShopId(shopId, pageable);
        return buildPagedApiResponse(productPage);
    }

    @GetMapping("/cards")
    public ApiResponse<PagedResponse<ProductCardResponse>> findProductCards(
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size

    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<ProductCardResponse> productPage = productService.findProductCards(categoryId, pageable);
        return buildPagedApiResponse(productPage);
    }

    @GetMapping("/shop/table/{shopId}")
    public ApiResponse<PagedResponse<ProductTableRowResponse>> findProductsForShopTable(
            @PathVariable Long shopId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<ProductTableRowResponse> productPage = productService.findProductsForSellerTable(shopId, pageable);
        return buildPagedApiResponse(productPage);
    }

    @GetMapping("/admin/pending-approval")
    public ApiResponse<PagedResponse<ProductTableRowResponse>> findPendingApprovalProductsForAdminTable(

            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<ProductTableRowResponse> productPage = productService.findPendingApprovalProductsForAdminTable(pageable);
        return buildPagedApiResponse(productPage);
    }

    @GetMapping("/admin/hidden")
    public ApiResponse<PagedResponse<ProductTableRowResponse>> findHiddenProductsForAdminTable(

            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<ProductTableRowResponse> productPage = productService.findHiddenProductsForAdminTable(pageable);
        return buildPagedApiResponse(productPage);
    }

    @GetMapping("/shop/{shopId}/pending-approval")
    public ApiResponse<PagedResponse<ProductTableRowResponse>> findPendingApprovalProductsForShopTable(
            @PathVariable Long shopId,
            @PageableDefault(size = 10) Pageable pageable

    ) {
        Page<ProductTableRowResponse> productPage = productService.findPendingApprovalProductsForShopTable(shopId, pageable);
        return buildPagedApiResponse(productPage);
    }

    private <T> ApiResponse<PagedResponse<T>> buildPagedApiResponse(Page<T> page) {
        PagedResponse<T> response = PagedResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();

        return ApiResponse.<PagedResponse<T>>builder()
                .result(response)
                .build();
    }

    @PostMapping
    public ApiResponse<ProductResponse> createProduct(@RequestBody @Valid ProductCreationRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.createProduct(request))
                .build();
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<String> deleteProduct(@PathVariable("productId") Long productId) {
        productService.deleteProduct(productId);
        return ApiResponse.<String>builder()
                .result("Product has been deleted").build();
    }

    @PutMapping("/{productId}")
    public ApiResponse<ProductResponse> updateProduct(@PathVariable("productId") Long productId, @RequestBody @Valid ProductUpdateRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.updateProduct(productId, request))
                .build();
    }

    @PostMapping("/{productId}/approve")
    public ApiResponse<ProductResponse> approveProduct(
            @PathVariable("productId") Long productId) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.approveProduct(productId))
                .build();
    }

    @PostMapping("/{productId}/reject")
    public ApiResponse<ProductResponse> rejectProduct(@PathVariable("productId") Long productId, @RequestBody ProductRejectRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.rejectProduct(productId, request))
                .build();
    }

    @PutMapping("/push/{productId}")
    public ApiResponse<ProductResponse> pushProduct(@PathVariable("productId") Long productId) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.pushProduct(productId))
                .build();
    }
}


