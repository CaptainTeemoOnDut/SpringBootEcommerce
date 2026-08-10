package com.longvo.demo_identity_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "product_variation",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"product_id", "name"})
        }
)
public class ProductVariation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code; // color, size

    @Column(nullable = false)
    private String name; // Mau sac, Kich thuoc

    @Column(nullable = false)
    Integer position = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToMany(
            mappedBy = "variation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ProductVariationOption> options = new ArrayList<>();
}

