package com.longvo.demo_identity_service.service;

import com.longvo.demo_identity_service.entity.Cart;
import com.longvo.demo_identity_service.entity.CartItem;
import com.longvo.demo_identity_service.entity.User;
import com.longvo.demo_identity_service.exception.AppException;
import com.longvo.demo_identity_service.exception.ErrorCode;
import com.longvo.demo_identity_service.repository.CartItemRepository;
import com.longvo.demo_identity_service.repository.CartRepository;
import com.longvo.demo_identity_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartSyncWorker {

    private final StringRedisTemplate redisTemplate;
    private final CartItemRepository cartItemRepository;
    private final CartRedisService cartRedisService; // Service bạn đã viết ở bước trước
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    @Scheduled(fixedDelay = 300000)
    public void syncCartToDb() {
        Set<String> dirtyUserIds = redisTemplate.opsForSet().members("dirty_carts");
        if (dirtyUserIds == null || dirtyUserIds.isEmpty()) return;

        for (String userIdStr : dirtyUserIds) {
            Long userId = Long.valueOf(userIdStr);

            // Gọi hàm tạo mới hoặc lấy ID hiện có
            Long cartId = createNewCartForUser(userId);

            Map<String, Integer> cartItems = cartRedisService.getCart(userId);

            if (cartItems.isEmpty()) {
                cartItemRepository.deleteAllByCartId(cartId);
            } else {
                // Upsert và Delete như các bước trước đã làm
                cartItems.forEach((skuId, quantity) -> {
                    cartItemRepository.upsertCartItem(cartId, Long.valueOf(skuId), quantity);
                });

                List<Long> currentSkuIds = cartItems.keySet().stream()
                        .map(Long::valueOf)
                        .collect(Collectors.toList());
                cartItemRepository.deleteItemsNotInList(cartId, currentSkuIds);
            }

            redisTemplate.opsForSet().remove("dirty_carts", userIdStr);
        }
    }

    @Transactional
    public Long createNewCartForUser(Long userId) {
        // 1. Kiểm tra lại một lần nữa trong DB để chắc chắn (Double Check)
        return cartRepository.findByUserId(userId)
                .map(Cart::getId)
                .orElseGet(() -> {
                    try {
                        // 2. Nếu chưa có, tiến hành tạo mới
                        Cart newCart = new Cart();
                        User user = userRepository.findById(userId)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
                        newCart.setUser(user);
                        Cart savedCart = cartRepository.saveAndFlush(newCart);
                        return savedCart.getId();
                    } catch (DataIntegrityViolationException e) {
                        // 3. Phòng trường hợp luồng khác vừa tạo xong ngay trước đó
                        // gây lỗi Unique Constraint
                        return cartRepository.findByUserId(userId)
                                .map(Cart::getId)
                                .orElseThrow(() -> new RuntimeException("Lỗi không thể tạo giỏ hàng"));
                    }
                });
    }
}
