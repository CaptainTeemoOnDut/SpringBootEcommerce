package com.longvo.demo_identity_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.longvo.demo_identity_service.enums.AttributeType;
import com.longvo.demo_identity_service.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "attribute")
public class Attribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;     // author, battery

    private String name;

    @Enumerated(EnumType.STRING)
    private AttributeType dataType;

    private String unit;
}

