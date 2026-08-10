package com.longvo.demo_identity_service.service;

import com.longvo.demo_identity_service.dto.response.CountryResponse;
import com.longvo.demo_identity_service.entity.*;
import com.longvo.demo_identity_service.enums.CartStatus;
import com.longvo.demo_identity_service.exception.AppException;
import com.longvo.demo_identity_service.exception.ErrorCode;
import com.longvo.demo_identity_service.mapper.CountryMapper;
import com.longvo.demo_identity_service.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartService {

    CartRepository cartRepository;
    CartItemRepository cartItemRepository;
    ProductVariantRepository productvariantRepository;
    UserRepository userRepository;

    @Transactional
    public void addToCart(Long userId, Long variantId, int quantity) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> createNewCart(userId));

        ProductVariant variant = productvariantRepository.findByIdWithDetails(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        // 1. SỬA LỖI: Tìm theo VariantId thay vì ProductId
        CartItem item = cartItemRepository
                .findByCartIdAndVariantId(cart.getId(), variantId)
                .orElse(null);

        if (item == null) {
            // 2. TỐI ƯU: Logic lấy ảnh đại diện
            String imageUrl = variant.getProduct().getThumbnailUrl();

            // Tìm ảnh từ các option (ví dụ ưu tiên ảnh của Màu sắc)
            imageUrl = variant.getAttributes().stream()
                    .flatMap(attr -> attr.getOption().getMedias().stream())
                    .map(ProductVariationOptionMedia::getPublicUrl)
                    .filter(url -> url != null && !url.isEmpty())
                    .findFirst() // Lấy ảnh đầu tiên tìm thấy
                    .orElse(imageUrl);

            cartItemRepository.save(
                    CartItem.builder()
                            .cart(cart)
                            .variant(variant) // QUAN TRỌNG: Phải lưu variant vào đây
                            .imageUrl(imageUrl)
                            .quantity(quantity)
                            .priceSnapshot(variant.getPrice())
                            .build()
            );
        } else {
            // 3. KIỂM TRA: Nên check tồn kho (stock) trước khi cộng dồn
            int newQuantity = item.getQuantity() + quantity;
            if (newQuantity > variant.getStock()) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }
            item.setQuantity(newQuantity);
            // Lưu ý: item đã là managed entity nên không nhất thiết phải gọi save() thủ công nếu dùng @Transactional
        }
    }

    private Cart createNewCart(Long userId) {
        Cart cart = new Cart();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        cart.setUser(user);
        cartRepository.save(cart);
        return cart;
    }
}
