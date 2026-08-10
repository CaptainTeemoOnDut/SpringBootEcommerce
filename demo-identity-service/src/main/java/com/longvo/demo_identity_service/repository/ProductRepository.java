package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.dto.response.ProductCardResponse;
import com.longvo.demo_identity_service.dto.response.ProductDetailResponse;
import com.longvo.demo_identity_service.dto.response.ProductTableRowResponse;
import com.longvo.demo_identity_service.entity.Product;
import com.longvo.demo_identity_service.entity.ProductCategory;
import com.longvo.demo_identity_service.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product>, ProductRepositoryCustom {
    Page<Product> findByCategoryId(@Param("id") Long id, Pageable pageable);

    Page<Product> findByNameContaining(@Param("name") String name, Pageable pageable);

    Page<ProductTableRowResponse> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByShopId(String shop_id, Pageable pageable);

    @Query("""
    select new com.longvo.demo_identity_service.dto.response.ProductDetailResponse(
        p.id,
        p.name,
        p.description,
        p.unitPrice,
        p.thumbnailUrl,
        p.unitsInStock,
        s.id,
        s.name,
        s.avatarUrl,
        c.id
    )
    from Product p
    join p.shop s
    join p.category c
    where p.id = :productId
""")
    ProductDetailResponse getProductDetails(@Param("productId") Long productId);


    @Query(
            value = """
        SELECT new com.longvo.demo_identity_service.dto.response.ProductCardResponse(
            p.id,
            p.name,
            p.thumbnailUrl,
            p.unitPrice,
            p.soldCount,
            s.isMall
        )
        FROM Product p
        JOIN p.shop s
        JOIN CategoryHierarchy ch ON p.category.id = ch.descendant.id
        WHERE ch.ancestor.id = :categoryId
          AND p.status = :status
    """,
            countQuery = """
        SELECT count(p.id)
        FROM Product p
        JOIN CategoryHierarchy ch ON p.category.id = ch.descendant.id
        WHERE ch.ancestor.id = :categoryId
          AND p.status = :status
    """
    )
    Page<ProductCardResponse> findProductCards(
            @Param("categoryId") Long categoryId,
            @Param("status") ProductStatus status,
            Pageable pageable
    );


    @Query(
                value = """
        select new com.longvo.demo_identity_service.dto.response.ProductTableRowResponse(
            p.id,
            p.name,
            p.sku,
            p.status,
            p.unitPrice,
            p.unitsInStock
        )
        from Product p
        where p.shop.id = :shopId
     """,
                countQuery = """
        select count(p.id)
        from Product p
        where p.shop.id = :shopId
     """
        )
    Page<ProductTableRowResponse> findProductsForSellerTable(
            @Param("shopId") Long shopId,
            Pageable pageable
    );


    @Query(
            value = """
        select new com.longvo.demo_identity_service.dto.response.ProductTableRowResponse(
            p.id,
            p.name,
            p.sku,
            p.status,
            p.unitPrice,
            p.unitsInStock
        )
        from Product p
        where p.status = :status
     """,
            countQuery = """
        select count(p.id)
        from Product p
        where p.status = :status
     """
    )
    Page<ProductTableRowResponse> findProductsForAdminTable(
            @Param("status") ProductStatus status,
            Pageable pageable
    );

    @Query(
            value = """
        select new com.longvo.demo_identity_service.dto.response.ProductTableRowResponse(
            p.id,
            p.name,
            p.sku,
            p.status,
            p.unitPrice,
            p.unitsInStock
        )
        from Product p
        where p.status = :status
            and p.shop.id = :shopId
     """,
            countQuery = """
        select count(p.id)
        from Product p
        where p.status = :status
            and p.shop.id = :shopId
     """
    )
    Page<ProductTableRowResponse> findPendingApprovalProductsForShopTable(
            @Param("shopId") Long shopId,
            @Param("status") ProductStatus status,
            Pageable pageable
    );

    @Query(
            value = """
        select new com.longvo.demo_identity_service.dto.response.ProductTableRowResponse(
            p.id,
            p.name,
            p.sku,
            p.status,
            p.unitPrice,
            p.unitsInStock
        )
        from Product p
        where p.status = :status
            and p.shop.id = :shopId
     """,
            countQuery = """
        select count(p.id)
        from Product p
        where p.status = :status
            and p.shop.id = :shopId
     """
    )
    Page<ProductTableRowResponse> findHiddenProductsForShopTable(
            @Param("shopId") Long shopId,
            @Param("status") ProductStatus status,
            Pageable pageable
    );

    @Modifying
    @Query("""
        UPDATE Product p
        SET p.avgRating = (
            SELECT s.avgRating
            FROM ProductRatingStats s
            WHERE s.productId = :productId
        )
        WHERE p.id = :productId
        """)
    void updateAvgRating(Long productId);

    Page<Product> findByShopIdAndStatus(Long shopId, ProductStatus status, Pageable pageable);
}
