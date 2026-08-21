package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.dto.response.OrderHistoryResponse;
import com.longvo.demo_identity_service.dto.response.ProductDetailResponse;
import com.longvo.demo_identity_service.entity.Order;
import com.longvo.demo_identity_service.entity.ProductVariant;
import com.longvo.demo_identity_service.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserEmail(@Param("email") String email, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "SELECT FROM Orders WHERE id IN (:orderIds)",
            nativeQuery = true)
    List<Order> findAllByIds (List<Long> orderIds);

    /*@Query("SELECT v FROM ProductVariant v " +
            "LEFT JOIN FETCH v.product p " +
            "LEFT JOIN FETCH p.shop " +
            "WHERE v.id = :id")
    List<Order> getOrderByOrderStatusAndUserId(@Param("orderStatus") OrderStatus orderStatus, @Param("userId") Long userId);*/

    /*@Query("""
        SELECT new com.longvo.demo_identity_service.dto.response.OrderHistoryResponse(
            o.orderGroup.id,
            s.name,
            o.totalAmount,
            o.status,
            o.createdAt
        )
        FROM Order o
        JOIN o.shop s
        WHERE o.user.id = :userId
        AND o.status = :status
        ORDER BY o.createdAt DESC,o.id DESC
        """)
            List<OrderHistoryResponse> findOrders(
                    @Param("userId") Long userId,
                    @Param("orderStatus") OrderStatus orderStatus

        );*/

    List<Order> findAllByOrderGroupId(Long orderGroupId);

    @Modifying
    @Query("""
    UPDATE Order o
    SET o.status = :status
    WHERE o.orderGroup.id = :orderGroupId
""")
    int updateStatusByOrderGroupId(
            Long orderGroupId,
            OrderStatus status
    );

    @Query("""
    SELECT new com.longvo.demo_identity_service.dto.response.OrderHistoryResponse(
            o.id,
            o.orderGroup.id,
            s.name,
            o.totalAmount,
            o.status,
            o.createdAt
    )
    FROM Order o
    JOIN o.shop s
    WHERE o.orderGroup.id IN :orderGroupIds
""")
    List<OrderHistoryResponse> findOrdersByOrderGroupIds(@Param("orderGroupIds") List<Long> orderGroupIds);
}
