package com.devteria.chat.repository.httpclient;

import com.devteria.chat.dto.ApiResponse;
import com.devteria.chat.dto.request.IntrospectRequest;
import com.devteria.chat.dto.response.IntrospectResponse;
import com.devteria.chat.dto.response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-service", url = "${app.services.identity.url}")
public interface ProductClient {
    //@GetMapping("/internal/users/{userId}")
    @GetMapping("/products/name/{productName}")
    ApiResponse<Page<ProductResponse>> findByNameContainingIgnoreCase(@PathVariable String productName);
}
