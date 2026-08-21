package com.longvo.demo_identity_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.longvo.demo_identity_service.dto.request.CartItemRequest;
import com.longvo.demo_identity_service.dto.response.CartItemResponse;
import com.longvo.demo_identity_service.dto.response.CartResponse;
import com.longvo.demo_identity_service.dto.response.ProductVariantResponse;
import com.longvo.demo_identity_service.entity.CartItem;
import com.longvo.demo_identity_service.entity.ProductVariant;
import com.longvo.demo_identity_service.exception.AppException;
import com.longvo.demo_identity_service.exception.ErrorCode;
import com.longvo.demo_identity_service.mapper.ProductVariantMapper;
import com.longvo.demo_identity_service.repository.CartItemRepository;
import com.longvo.demo_identity_service.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartRedisService {

    private final StringRedisTemplate redisTemplate;
    private final ProductVariantRepository productVariantRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantMapper productVariantMapper;

    private String getCartKey(Long userId) {
        return "cart:" + userId;
    }

    private String getCartField(Long productId) {
        return "product:" + productId;
    }


    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    public CartItemResponse updateCartRedis(Long userId, CartItemRequest cartItemRequest) {
        String skuIdStr = cartItemRequest.getSkuId().toString();
        String key = getCartKey(userId);

        // 1. Validate Sku
        ProductVariantResponse productVariant = getVariantWithCache(cartItemRequest.getSkuId());

        // 2. Tính toán số lượng mục tiêu (Target Quantity)
        int targetQuantity;
        if (cartItemRequest.isReplace()) {
            // Trường hợp user gõ trực tiếp số lượng hoặc sửa trong giỏ hàng
            targetQuantity = cartItemRequest.getQuantity();
        } else {
            // Trường hợp nhấn "Add to cart" ở trang ngoài (cộng dồn)
            Object currentQtyObj = redisTemplate.opsForHash().get(key, skuIdStr);
            int currentQtyInCart = (currentQtyObj != null) ? Integer.parseInt(currentQtyObj.toString()) : 0;
            targetQuantity = currentQtyInCart + cartItemRequest.getQuantity();
        }

        // 3. Check Stock
        if (targetQuantity > productVariant.getStock()) {
            throw new AppException(ErrorCode.OUT_OF_STOCK);
        }

        if (targetQuantity <= 0) {
            redisTemplate.opsForHash().delete(key, skuIdStr);
        } else {
            // 4. Ghi vào Redis (Dùng put để xác định con số chính xác cuối cùng)
            redisTemplate.opsForHash().put(key, skuIdStr, String.valueOf(targetQuantity));

            // 5. Mark Dirty
            redisTemplate.opsForSet().add("dirty_carts", userId.toString());
        }

        // 6. Trả về Response với số lượng thực tế trong giỏ
        return CartItemResponse.builder()
                .variantId(productVariant.getVariantId())
                .name(productVariant.getName())
                .imageUrl(productVariant.getImageUrl())
                .unitPrice(productVariant.getUnitPrice())
                .quantity(targetQuantity) // Trả về con số cuối cùng
                .skuSnapShot(productVariant.getSku())
                .shopName(productVariant.getShopName())
                .shopId(productVariant.getShopId())
                .selected(false)
                .build();
    }



    public ProductVariantResponse getVariantWithCache(Long skuId) {
        String cacheKey = "product:variant:" + skuId;

        // 1. Thử lấy từ Redis trước
        Object cachedData = redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            System.out.println(cachedData);
            System.out.println("GET DATA FROM REDIS...HAHA");
            return convertJsonToEntity(cachedData); // Trả về ngay, không query DB
        }

        // 2. Nếu không có, mới vào DB
        ProductVariant variant = productVariantRepository.findByCartItemId(skuId);

        ProductVariantResponse productVariantResponse = ProductVariantResponse.builder()
                .variantId(variant.getId())
                .name(variant.getProduct().getName())
                .imageUrl(variant.getImageUrl())
                .unitPrice(variant.getPrice())
                .stock(variant.getStock())
                .sku(variant.getSku())
                .shopName(variant.getProduct().getShop().getName())
                .shopId(variant.getProduct().getShop().getId())
                .build();

        // 3. Lưu vào Redis với TTL (ví dụ 5-10 phút) để tránh dữ liệu quá cũ
        redisTemplate.opsForValue().set(cacheKey, convertEntityToJson(productVariantResponse), Duration.ofMinutes(10));

        return productVariantResponse;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 1. Chuyển Object thành String JSON để lưu vào Redis
    private String convertEntityToJson(ProductVariantResponse variant) {
        try {
            return objectMapper.writeValueAsString(variant);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lỗi khi chuyển Entity sang JSON", e);
        }
    }

    // 2. Chuyển String JSON từ Redis ngược lại thành Object
    private ProductVariantResponse convertJsonToEntity(Object cachedData) {
        try {
            // cachedData từ Redis thường là String hoặc có thể ép kiểu về String
            return objectMapper.readValue(cachedData.toString(), ProductVariantResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lỗi khi chuyển JSON sang Entity", e);
        }
    }

    public CartResponse getUserCart(Long userId) {
        String key = getCartKey(userId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        // --- BẮT ĐẦU LOGIC FALLBACK ---
        if (entries.isEmpty()) {
            // 1. Nếu Redis trống, thử tìm trong DB xem có data đã sync trước đó không
            List<CartItem> dbItems = cartItemRepository.findAllByUserId(userId);
            if (dbItems.isEmpty()) return CartResponse.builder().build();

            // 2. Nạp ngược lại vào Redis để lần sau không phải vào DB nữa
            dbItems.forEach(item ->
                    redisTemplate.opsForHash().put(key, item.getVariant().getId().toString(), String.valueOf(item.getQuantity()))
            );

            // 3. Giả lập entries để chạy tiếp logic bên dưới
            entries = dbItems.stream().collect(Collectors.toMap(
                    item -> item.getVariant().getId(),
                    item -> item.getQuantity()
            ));
        }
        // --- KẾT THÚC LOGIC FALLBACK ---

        Map<Long, Integer> cartItemsMap = entries.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> Long.parseLong(e.getKey().toString()),
                        e -> Integer.parseInt(e.getValue().toString())
                ));

        List<Long> variantIds = new ArrayList<>(cartItemsMap.keySet());

        // Query thông tin sản phẩm "tươi" nhất từ bảng Products/Variants
        List<CartItemResponse> cartItemResponses = productVariantRepository.findProductsByVariantIds(variantIds);

        cartItemResponses.forEach(response -> {
            Integer qty = cartItemsMap.get(response.getVariantId());
            if (qty != null) response.setQuantity(qty);
        });

        return CartResponse.builder()
                .cartItemResponseList(cartItemResponses)
                .build();
    }

    /**
     * Lấy toàn bộ giỏ hàng của User (chỉ gồm skuId và quantity)
     */
    public Map<String, Integer> getCart(Long userId) {
        String key = getCartKey(userId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        // Convert Map<Object, Object> sang Map<String, Integer> để dễ xử lý ở tầng trên
        return entries.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().toString(),
                        e -> Integer.parseInt(e.getValue().toString())
                ));
    }

    public CartItemResponse addToRedis(Long userId, CartItemRequest cartItemRequest) {
        // 1. Validate Sku tồn tại
        ProductVariant productVariant = productVariantRepository.findByCartItemId(cartItemRequest.getSkuId());

        // 2. Lấy số lượng hiện tại đang có trong giỏ hàng (Redis)
        String key = getCartKey(userId);
        Object currentQtyObj = redisTemplate.opsForHash().get(key, cartItemRequest.getSkuId().toString());
        int currentQtyInCart = (currentQtyObj != null) ? Integer.parseInt(currentQtyObj.toString()) : 0;

        // 3. Check Stock: Tổng (đang có + sắp thêm) không được vượt quá tồn kho
        if (productVariant.getStock() < (currentQtyInCart + cartItemRequest.getQuantity())) {
            throw new AppException(ErrorCode.OUT_OF_STOCK);
        }

        // 4. Update Redis (Dùng increment để đảm bảo Atomic)
        redisTemplate.opsForHash().increment(key, cartItemRequest.getSkuId().toString(), cartItemRequest.getQuantity());

        // 5. Mark Dirty
        redisTemplate.opsForSet().add("dirty_carts", userId.toString());

        return CartItemResponse.builder()
                .variantId(productVariant.getId())
                .name(productVariant.getProduct().getName())
                .imageUrl(productVariant.getImageUrl())
                .unitPrice(productVariant.getPrice())
                .quantity(cartItemRequest.getQuantity())
                .skuSnapShot(productVariant.getSku())
                .shopName(productVariant.getProduct().getShop().getName())
                .shopId(productVariant.getProduct().getShop().getId())
                .selected(false)
                .build();
    }

    /**
     * Xóa một sản phẩm khỏi giỏ hàng
     */
    public void removeFromCart(Long userId, String skuId) {
        String key = getCartKey(userId);
        redisTemplate.opsForHash().delete(key, skuId);
    }

    /**
     * Xóa toàn bộ giỏ hàng (Sau khi thanh toán thành công)
     */
    public void clearCart(Long userId) {
        String key = getCartKey(userId);
        redisTemplate.delete(key);
    }
}

