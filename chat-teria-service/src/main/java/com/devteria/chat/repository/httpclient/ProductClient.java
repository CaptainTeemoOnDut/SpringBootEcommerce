package com.devteria.chat.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.devteria.chat.dto.ApiResponse;
import com.devteria.chat.dto.response.ProductResponse;

@FeignClient(name = "product-service", url = "${app.services.identity.url}")
public interface ProductClient {
    // @GetMapping("/internal/users/{userId}")
    @GetMapping("/products/name/{productName}")
    ApiResponse<Page<ProductResponse>> findByNameContainingIgnoreCase(@PathVariable String productName);
}
