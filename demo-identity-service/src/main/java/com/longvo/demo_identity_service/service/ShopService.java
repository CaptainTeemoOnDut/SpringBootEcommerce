package com.longvo.demo_identity_service.service;

import com.longvo.demo_identity_service.dto.request.ShopCreationRequest;
import com.longvo.demo_identity_service.dto.response.ShopResponse;
import com.longvo.demo_identity_service.entity.Product;
import com.longvo.demo_identity_service.entity.Shop;
import com.longvo.demo_identity_service.entity.User;
import com.longvo.demo_identity_service.enums.ShopStatus;
import com.longvo.demo_identity_service.exception.AppException;
import com.longvo.demo_identity_service.exception.ErrorCode;
import com.longvo.demo_identity_service.mapper.ShopMapper;
import com.longvo.demo_identity_service.repository.ProductRepository;
import com.longvo.demo_identity_service.repository.ShopRepository;
import com.longvo.demo_identity_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopService {

    private final ShopMapper shopMapper;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ShopResponse createShop(ShopCreationRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (shopRepository.existsByUser_Id(user.getId())) {
            throw new AppException(ErrorCode.USER_ALREADY_HAS_SHOP);
        }

        Shop shop = shopMapper.toShop(request);
        shop.setUser(user);
        shop.setStatus(ShopStatus.ACTIVE);

        try {
            shop = shopRepository.save(shop);
        } catch (DataIntegrityViolationException e) {
            log.warn("Shop name duplicated: {}", request.getName(), e);
            throw new AppException(ErrorCode.SHOP_EXISTED);
        }

        return shopMapper.toShopResponse(shop);
    }

    public Page<Product> getProductsByShopId(String shopId, Pageable pageable) {
        return productRepository.findByShopId(shopId, pageable);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ShopResponse suspendShop(Long shopId, String reason) {

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_EXISTED));

        shop.setIsActive(false);
        shop.setStatus(ShopStatus.SUSPENDED);
        shop.setSuspendedAt(LocalDateTime.now());
        shop.setSuspendedReason(reason);

        log.info("Admin suspended shop {} with reason: {}", shopId, reason);

        return shopMapper.toShopResponse(shop);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ShopResponse activateShop(Long shopId) {

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_EXISTED));

        shop.setIsActive(true);
        shop.setStatus(ShopStatus.ACTIVE);
        shop.setSuspendedAt(null);
        shop.setSuspendedReason(null);

        log.info("Admin activated shop {}", shopId);

        return shopMapper.toShopResponse(shop);
    }
}
