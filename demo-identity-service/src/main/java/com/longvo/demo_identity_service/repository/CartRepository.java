package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.entity.Cart;
import com.longvo.demo_identity_service.entity.CategoryAttribute;
import com.longvo.demo_identity_service.entity.interfaces.CategoryAttributeProjection;
import com.longvo.demo_identity_service.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("""
        SELECT c
        FROM Cart c
        WHERE c.user.id = :userId
          AND c.status = com.longvo.demo_identity_service.enums.CartStatus.ACTIVE
    """)
    Optional<Cart> findActiveCartByUser(@Param("userId") Long userId);

    Optional<Cart> findByUserIdAndStatus(Long userId, CartStatus status);

    Optional<Cart> findByUserId(Long userId);

}
