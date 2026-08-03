package com.devteria.chat.Tool;

import com.devteria.chat.dto.ApiResponse;
import com.devteria.chat.dto.response.ProductResponse;
import com.devteria.chat.repository.httpclient.ProductClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductTools {

    private final ProductClient productClient;

    public ProductTools( ProductClient productClient) {
        this.productClient = productClient;
    }

    @Tool(description = "Search products by keyword")
    public ApiResponse<Page<ProductResponse>> searchProducts(String keyword) {
        return productClient.findByNameContainingIgnoreCase(keyword);
    }

    /*@Tool(description = "Get stock of a product by id")
    public int getStock(Long productId) {
        return productRepository.findById(productId)
                .map(Product::getStock)
                .orElse(0);
    }*/
}
