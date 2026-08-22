package com.longvo.demo_identity_service.entity;

import com.longvo.demo_identity_service.enums.ShopStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false)
    String name;

    String avatarUrl;

    @OneToOne
    @JoinColumn(name = "owner_id", unique = true)
    private User owner;

    @Column(nullable = false)
    Boolean isActive = true;

    @Column(nullable = false)
    Boolean isMall = false;

    LocalDateTime suspendedAt;
    String suspendedReason;

    @Enumerated(EnumType.STRING)
    ShopStatus status = ShopStatus.ACTIVE;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    User user;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Product> products = new HashSet<>();

    @CreationTimestamp
    LocalDateTime createdAt;

    public void addProduct(Product product) {
        products.add(product);
        product.setShop(this);
    }
}
