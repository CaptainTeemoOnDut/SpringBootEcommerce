package com.longvo.demo_identity_service.entity;

import com.longvo.demo_identity_service.enums.OrderGroupStatus;
import com.longvo.demo_identity_service.enums.OrderStatus;
import com.longvo.demo_identity_service.enums.PaymentMethods;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "OrderGroup") // tên mới không trùng từ khóa
public class OrderGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderGroupStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethods paymentMethod; // VNPAY, COD

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime expiredAt;

    private LocalDateTime paidAt;

    @OneToMany(mappedBy = "orderGroup", cascade = CascadeType.ALL)
    private Set<Order> orders = new HashSet<>();

    @Version
    private Long version;

}
