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
@Table(name = "category_attribute")
public class CategoryAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private ProductCategory category;

    @ManyToOne
    private Attribute attribute;

    private boolean required;
}

