package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.dto.response.OrderGroupHistoryResponse;
import com.longvo.demo_identity_service.dto.response.OrderHistoryResponse;
import com.longvo.demo_identity_service.entity.Order;
import com.longvo.demo_identity_service.entity.OrderGroup;
import com.longvo.demo_identity_service.enums.OrderGroupStatus;
import com.longvo.demo_identity_service.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderGroupRepository extends JpaRepository<OrderGroup, Long> {
    /*@Query("""
        SELECT new com.longvo.demo_identity_service.dto.response.OrderGroupHistoryResponse(
            o.id,
            o.createdAt
        )
        FROM OrderGroup o
        WHERE o.user.id = :userId
        AND o.status = :orderStatus
        ORDER BY o.createdAt DESC, o.id DESC
        """)
    List<OrderGroupHistoryResponse> findOrderGroup(@Param("userId") Long userId, @Param("orderStatus") OrderStatus orderStatus);*/

    @Query("""
    SELECT new com.longvo.demo_identity_service.dto.response.OrderGroupHistoryResponse(
        o.id,
        o.createdAt
    )
    FROM OrderGroup o
    WHERE o.user.id = :userId
    AND o.status = :orderGroupStatus
    ORDER BY o.createdAt DESC, o.id DESC
""")
    Page<OrderGroupHistoryResponse> findOrderGroups(
            @Param("userId") Long userId,
            @Param("orderGroupStatus") OrderGroupStatus orderGroupStatus,
            Pageable pageable
    );

    @Query("""
    SELECT new com.longvo.demo_identity_service.dto.response.OrderGroupHistoryResponse(
        o.id,
        o.createdAt
    )
    FROM OrderGroup o
    WHERE o.user.id = :userId
    ORDER BY o.createdAt DESC, o.id DESC
""")
    Page<OrderGroupHistoryResponse> findOrderGroupsByUserId(Long userId, Pageable pageable);

}



