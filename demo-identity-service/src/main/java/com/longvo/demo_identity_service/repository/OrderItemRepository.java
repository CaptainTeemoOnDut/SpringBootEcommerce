package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.dto.response.OrderItemResponse;
import com.longvo.demo_identity_service.entity.Order;
import com.longvo.demo_identity_service.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
        SELECT new com.longvo.demo_identity_service.dto.response.OrderItemResponse(
            oi.order.id,
            oi.id,
            oi.variant.id,
            oi.variant.product.name,
            oi.variant.imageUrl,
            oi.skuSnapshot,
            oi.unitPrice,
            oi.quantity
            
        )
        FROM OrderItem oi
        WHERE oi.order.id IN :orderIds
        ORDER BY oi.order.id, oi.id
        """)
            List<OrderItemResponse> findItems(@Param("orderIds") List<Long> orderIds);

    @Query("""
    SELECT oi FROM OrderItem oi
    LEFT JOIN FETCH oi.variant v
    LEFT JOIN FETCH v.product p
    LEFT JOIN FETCH oi.order o
    LEFT JOIN FETCH o.user
    WHERE oi.id = :id
""")
    Optional<OrderItem> findById(@Param("id") Long id);
}
