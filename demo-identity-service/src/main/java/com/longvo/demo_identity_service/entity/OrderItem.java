package com.longvo.demo_identity_service.entity;


import com.longvo.demo_identity_service.enums.OrderItemStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        indexes = {
                @Index(name = "idx_order_item_sku", columnList = "sku_id"),
                @Index(name = "idx_order_item_order", columnList = "order_id")
        },
        uniqueConstraints = {
        @UniqueConstraint(columnNames = {"order_id", "sku_id"})
        }
)

public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;*/

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(name = "sku_id")
    private Long skuId;

    @Column(name = "sku_snapshot")
    private String skuSnapshot; // Color: White + Size: XL

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "quantity")
    private int quantity;

    @Enumerated(EnumType.STRING)
    OrderItemStatus status;
// PURCHASED, RETURNED, REFUNDED


    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @OneToMany(mappedBy = "orderItem",cascade = CascadeType.ALL)
    Set<Review> reviews = new HashSet<>();
}
