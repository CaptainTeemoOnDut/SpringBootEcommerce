package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.dto.request.ProductSearchRequest;
import com.longvo.demo_identity_service.dto.response.ProductCardResponse;
import com.longvo.demo_identity_service.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {
    Page<ProductCardResponse> searchProducts(ProductSearchRequest request, Pageable pageable);
}
