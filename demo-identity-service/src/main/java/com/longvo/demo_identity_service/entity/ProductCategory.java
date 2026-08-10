package com.longvo.demo_identity_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "product_category",
        indexes = {
                @Index(name = "idx_category_parent", columnList = "parent_id")
        }
)
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "category_name", nullable = false)
    String name;

    @Column(name = "image_url")
    String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    ProductCategory parent;

    /*@Column(name = "is_leaf", nullable = false)
    Boolean isLeaf;*/


    // ❌ KHÔNG map products
}


/*public class ProductCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "category_name")
    String categoryName;

    @Column(name = "imageUrl")
    String imageUrl;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "category")
    Set<Product> products;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    ProductCategory parent;
}*/
