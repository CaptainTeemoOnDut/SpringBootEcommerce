package com.longvo.demo_identity_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "variant_attribute",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                        "variant_id",
                        "variation_id"
                })
        },
        indexes = {
                //@Index(name = "idx_variant_id", columnList = "product_variant_id")
        }
)
public class VariantAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Variant này
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    // Loại phân loại (Color, Size)
    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "variation_id")
    private ProductVariation variation;

    // Giá trị (Black, XL)
    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "option_id")
    private ProductVariationOption option;
}


