package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.dto.request.CartItemRequest;
import com.longvo.demo_identity_service.dto.response.CartItemResponse;
import com.longvo.demo_identity_service.dto.response.ProductDetailResponse;
import com.longvo.demo_identity_service.entity.Cart;
import com.longvo.demo_identity_service.entity.CartItem;
import com.longvo.demo_identity_service.entity.CategoryAttribute;
import com.longvo.demo_identity_service.entity.Product;
import com.longvo.demo_identity_service.entity.interfaces.CategoryAttributeProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    //Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    Optional<CartItem> findByCartIdAndVariantId(Long cartId, Long variantId);

    @Query("SELECT ci FROM CartItem ci " +
            "JOIN FETCH ci.cart v " +
            "WHERE v.user.id = :userId")
    List<CartItem> findAllByUserId(Long userId);

    @Query("SELECT ci FROM CartItem ci " +
            "JOIN FETCH ci.variant v " +
            "JOIN FETCH v.product p " +
            "JOIN FETCH p.shop s " +
            "WHERE v.id IN :ids")
    List<CartItem> findAllWithProductAndShopByIds(List<Long> ids);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO cart_items (cart_id, sku_id, quantity) " +
            "VALUES (:cartId, :skuId, :quantity) " +
            "ON DUPLICATE KEY UPDATE quantity = :quantity", nativeQuery = true)
    void upsertCartItem(@Param("cartId") Long cartId,
                        @Param("skuId") Long skuId,
                        @Param("quantity") int quantity);

    // Tìm hoặc tạo mới Cart cho User
    @Query("SELECT c.id FROM Cart c WHERE c.user.id = :userId")
    Optional<Long> findCartIdByUserId(@Param("userId") Long userId);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM cart_items WHERE cart_id = :cartId AND sku_id NOT IN (:listSkuIds)",
            nativeQuery = true)
    void deleteItemsNotInList(@Param("cartId") Long cartId,
                              @Param("listSkuIds") List<Long> listSkuIds);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM cart_items WHERE cart_id = :cartId",
            nativeQuery = true)
    void deleteAllByCartId (Long cartId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM cart_items WHERE cart_id IN (:cartIds)",
            nativeQuery = true)
    void removeFromCart (List<Long> cartIds);



}
