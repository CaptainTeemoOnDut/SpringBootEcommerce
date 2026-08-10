package com.longvo.demo_identity_service.entity;

import com.longvo.demo_identity_service.enums.AttributeType;
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
@Table(name = "product_attribute_value")
public class ProductAttributeValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Product product;

    @ManyToOne
    private Attribute attribute;

    private String valueString;
    private Double valueNumber;
    private Boolean valueBoolean;
}

