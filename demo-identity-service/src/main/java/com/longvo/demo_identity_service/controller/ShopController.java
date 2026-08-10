package com.longvo.demo_identity_service.controller;

import com.longvo.demo_identity_service.dto.request.ProductCreationRequest;
import com.longvo.demo_identity_service.dto.request.ProductUpdateRequest;
import com.longvo.demo_identity_service.dto.request.ShopCreationRequest;
import com.longvo.demo_identity_service.dto.response.*;
import com.longvo.demo_identity_service.entity.Product;
import com.longvo.demo_identity_service.service.ProductService;
import com.longvo.demo_identity_service.service.ShopService;
import com.longvo.demo_identity_service.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ShopController {

    @Autowired
    ShopService shopService;
    @Autowired
    private UserService userService;

    @GetMapping("/category")
    public ApiResponse<PagedResponse<Product>> getProductsByShopId(
            @RequestParam String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = shopService.getProductsByShopId(id, pageable);
        return getPagedResponseApiResponse(productPage);
    }

    private ApiResponse<PagedResponse<Product>> getPagedResponseApiResponse(Page<Product> productPage) {
        PagedResponse<Product> response = PagedResponse.<Product>builder()
                .content(productPage.getContent())
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .build();
        return ApiResponse.<PagedResponse<Product>>builder()
                .result(response)
                .build();
    }

    @PostMapping
    public ApiResponse<ShopResponse> createShop(@RequestBody @Valid ShopCreationRequest request) {
        return ApiResponse.<ShopResponse>builder()
                .result(shopService.createShop(request))
                .build();
    }

    @PutMapping("suspend/{shopId}")
    ApiResponse<ShopResponse> suspendShop(@PathVariable("shopId") Long shopId, String reason) {
        return ApiResponse.<ShopResponse>builder()
                .result(shopService.suspendShop(shopId, reason))
                .build();
    }

    @PutMapping("activate/{shopId}")
    ApiResponse<ShopResponse> activateShop(@PathVariable("shopId") Long shopId) {
        return ApiResponse.<ShopResponse>builder()
                .result(shopService.activateShop(shopId))
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


