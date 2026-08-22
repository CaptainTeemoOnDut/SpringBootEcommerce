package com.longvo.demo_identity_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
        name = "product_variation_option",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"variation_id", "option_value"})
        }
)
public class ProductVariationOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "option_value", nullable = false)
    private String value; // Black, XL

    @Column(nullable = false)
    Integer position = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "variation_id")
    private ProductVariation variation;

    @OneToMany(
            mappedBy = "productVariationOption",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ProductVariationOptionMedia> medias = new ArrayList<>();
}


