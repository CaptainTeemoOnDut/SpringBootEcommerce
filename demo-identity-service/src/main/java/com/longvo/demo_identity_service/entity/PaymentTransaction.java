package com.longvo.demo_identity_service.entity;

import com.longvo.demo_identity_service.enums.OrderStatus;
import com.longvo.demo_identity_service.enums.PaymentMethods;
import com.longvo.demo_identity_service.enums.PaymentStatus;
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
@Table(name = "payment_transaction")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_group_id", nullable = false)
    private OrderGroup orderGroup;

    @Enumerated(EnumType.STRING)
    private PaymentMethods paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(nullable = false, unique = true)
    private String vnpTxnRef;

    private BigDecimal totalAmount;

    private String vnpTransactionNo;

    private String bankCode;

    private String responseCode;

    private String transactionStatus;

    private LocalDateTime paidAt;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String rawResponse;

    @CreationTimestamp
    private LocalDateTime createdAt;
}