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
@Table(name = "category_hierarchy", indexes = {
        // Index 1: Tối ưu cho query tìm con cháu từ cha (Ancestor -> Descendant)
        @Index(name = "idx_hierarchy_ancestor_descendant", columnList = "ancestor_id, descendant_id"),
        // Index 2: Tối ưu cho phép JOIN ngược từ Product (Descendant -> Ancestor)
        @Index(name = "idx_hierarchy_descendant", columnList = "descendant_id")
})
public class CategoryHierarchy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ancestor_id", nullable = false)
    ProductCategory ancestor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "descendant_id", nullable = false)
    ProductCategory descendant;

    @Column(name = "path_length", nullable = false)
    Integer pathLength; // 0 là chính nó, 1 là con trực tiếp, 2 là cháu...
}


