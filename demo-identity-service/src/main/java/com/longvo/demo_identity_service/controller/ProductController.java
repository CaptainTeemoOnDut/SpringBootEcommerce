package com.longvo.demo_identity_service.controller;

import com.longvo.demo_identity_service.dto.request.*;
import com.longvo.demo_identity_service.dto.response.*;
import com.longvo.demo_identity_service.entity.Product;
import com.longvo.demo_identity_service.service.ProductService;
import com.longvo.demo_identity_service.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
public class ProductController {

    @Autowired
    ProductService productService;
    @Autowired
    ProductVariantService productVariantService;

    @GetMapping
    public Page<ProductCardResponse> getProducts(
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

        return productService.filterProductCards(
                request, page, size, sortBy, direction
        );


    }

    @GetMapping("/search")
    public Page<ProductCardResponse> searchProducts(
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

        return productService.search(request, pageable);
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

    @GetMapping("/product/{id}")
    public ApiResponse<ProductDetailResponse> getProductDetailById(
            @PathVariable Long id

    ) {
        return ApiResponse.<ProductDetailResponse>builder()
                .result(productService.getProductDetail(id))
                .build();
    }

    @GetMapping("/variants/{variantId}/revalidate")
    public ApiResponse<VariantInforDTO> revalidate(
            @PathVariable Long variantId

    ) {
        return ApiResponse.<VariantInforDTO>builder()
                .result(productVariantService.revalidate(variantId))
                .build();
    }

    @GetMapping("/shop/{id}")
    public ApiResponse<PagedResponse<ProductResponse>> getProductsByShopId(
            @PathVariable("id") String id,
            @PageableDefault(size = 10) Pageable pageable
            //@RequestParam(defaultValue = "0") int page,
            //@RequestParam(defaultValue = "10") int size
    ) {
        //Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> productPage = productService.getProductsByShopId(id, pageable);
        return buildPagedApiResponse(productPage);
    }

    @GetMapping("/product_card")
    public ApiResponse<Page<ProductCardResponse>> findProductCards(
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size

    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<ProductCardResponse> productPage = productService.findProductCards(categoryId, pageable);
        return ApiResponse.<Page<ProductCardResponse>>builder()
                .result(productPage)
                .build();
    }

    @GetMapping("/shop/table/{shopId}")
    public ApiResponse<PagedResponse<ProductTableRowResponse>> findProductsForShopTable(
            @PathVariable Long shopId,
            @PageableDefault(size = 10) Pageable pageable
            //@RequestParam(defaultValue = "0") int page,
            //@RequestParam(defaultValue = "10") int size
    ) {
        //Pageable pageable = PageRequest.of(page, size);

        Page<ProductTableRowResponse> productPage = productService.findProductsForSellerTable(shopId, pageable);
        return buildPagedApiResponse(productPage);
    }

    @GetMapping("/admin/pending-approval")
    public ApiResponse<PagedResponse<ProductTableRowResponse>> findPendingApprovalProductsForAdminTable(

            @PageableDefault(size = 10) Pageable pageable
            //@RequestParam(defaultValue = "0") int page,
            //@RequestParam(defaultValue = "10") int size
    ) {
        //Pageable pageable = PageRequest.of(page, size);

        Page<ProductTableRowResponse> productPage = productService.findPendingApprovalProductsForAdminTable(pageable);
        return buildPagedApiResponse(productPage);
    }

    @GetMapping("/admin/hidden")
    public ApiResponse<PagedResponse<ProductTableRowResponse>> findHiddenProductsForAdminTable(

            @PageableDefault(size = 10) Pageable pageable
            //@RequestParam(defaultValue = "0") int page,
            //@RequestParam(defaultValue = "10") int size
    ) {
        //Pageable pageable = PageRequest.of(page, size);

        Page<ProductTableRowResponse> productPage = productService.findHiddenProductsForAdminTable(pageable);
        return buildPagedApiResponse(productPage);
    }

    @GetMapping("/shop/{shopId}/pending-approval")
    public ApiResponse<PagedResponse<ProductTableRowResponse>> findPendingApprovalProductsForShopTable(
            @PathVariable Long shopId,
            @PageableDefault(size = 10) Pageable pageable
            //@RequestParam(defaultValue = "0") int page,
            //@RequestParam(defaultValue = "10") int size
    ) {
        //Pageable pageable = PageRequest.of(page, size);

        Page<ProductTableRowResponse> productPage = productService.findPendingApprovalProductsForShopTable(shopId, pageable);
        return buildPagedApiResponse(productPage);
    }

    /*@GetMapping("/shop/{id}/{status}")
    public ApiResponse<PagedResponse<ProductResponse>> getProductsByShopIdAndStatus(
            @PathVariable("id") String id,
            @PathVariable("status") ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> productPage = productService.getProductsByShopIdAndStatus(id,status, pageable);
        return getPagedResponseApiResponse(productPage);
    }*/

    /*@GetMapping()
    public ApiResponse<PagedResponse<ProductResponse>> getProductsByStatus(
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> productPage = productService.getPendingApprovalProduct(status, pageable);
        return getPagedResponseApiResponse(productPage);
    }*/

    /*@GetMapping("/search")
    public ApiResponse<PagedResponse<ProductResponse>> searchProductsByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> productPage = productService.searchProductsByName(name, pageable);
        return buildPagedApiResponse(productPage);
    }*/

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


    

    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProductById(@PathVariable("productId") Long productId) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getProductById(productId))
                .build();
    }

    @GetMapping("/name/{productName}")
    public ApiResponse<Page<ProductResponse>> getProductByName(
            @PathVariable("productName") String productName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Pageable pageable = PageRequest.of(page, size);
        var response = productService.searchProductsByName(productName, pageable);
        return ApiResponse.<Page<ProductResponse>>builder()
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
    public ApiResponse<ProductResponse> updateProduct(@PathVariable("productId") Long productId, @RequestBody ProductUpdateRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.updateProduct(productId, request))
                .build();
    }

    @PutMapping("/approve/{productId}")
    public ApiResponse<ProductResponse> approveProduct(
            @PathVariable("productId") Long productId) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.approveProduct(productId))
                .build();
    }

    @PutMapping("/reject/{productId}")
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

    /*public ApiResponse<Page<ProductResponse>> getProducts(
            @RequestParam(name = "id") Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = productService.getProducts(categoryId, pageable);

        return ApiResponse.<Page<ProductResponse>>builder()
                .code(1000)
                .message("Success")
                .result(products)
                .build();
    }*/
    /*public ResponseEntity<?> getProducts(@RequestParam Long id,
                                         @RequestParam int page,
                                         @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> pageResult = productService.getProducts(id, pageable);

        PagedResponse<ProductResponse> pagedResponse = new PagedResponse<>(
                pageResult.getContent(),
                pageResult.getTotalPages(),
                pageResult.getTotalElements(),
                pageResult.getSize(),
                pageResult.getNumber(),
                pageResult.isLast()
        );

        return ResponseEntity.ok(
                Map.of(
                        "code", 1000,
                        "message", "Success",
                        "result", pagedResponse

                )
        );
    }*/
}


