package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.entity.CategoryAttribute;
import com.longvo.demo_identity_service.entity.Country;
import com.longvo.demo_identity_service.entity.interfaces.CategoryAttributeProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute,Long> {
    List<CategoryAttribute> findByCategoryId(Long id);

    @Query(
            value = """
        WITH RECURSIVE category_tree AS (
            SELECT id, parent_id
            FROM product_category
            WHERE id = :categoryId

            UNION ALL

            SELECT pc.id, pc.parent_id
            FROM product_category pc
            JOIN category_tree ct ON pc.id = ct.parent_id
        )
        SELECT DISTINCT
            a.id               AS attributeId,
            a.code             AS code,
            a.name             AS name,
            a.data_type        AS dataType,
            a.unit             AS unit,
            ca.required        AS required,
            ca.category_id     AS categoryId
        FROM category_tree ct
        JOIN category_attribute ca ON ca.category_id = ct.id
        JOIN attribute a ON a.id = ca.attribute_id
        ORDER BY ca.category_id
        """,
            nativeQuery = true
    )
    List<CategoryAttributeProjection> findAllAttributesByCategoryTree(
            @Param("categoryId") Long categoryId
    );

    @Query(value = """
        WITH RECURSIVE category_tree AS (
            SELECT id, parent_id
            FROM product_category
            WHERE id = :categoryId
            UNION ALL
            SELECT pc.id, pc.parent_id
            FROM product_category pc
            JOIN category_tree ct ON pc.id = ct.parent_id
        )
        SELECT 
            a.id,
            a.code,
            a.name,
            a.data_type,
            a.unit,
            ca.required,
            ca.category_id
        FROM category_tree ct
        JOIN category_attribute ca ON ca.category_id = ct.id
        JOIN attribute a ON a.id = ca.attribute_id
    """, nativeQuery = true)
    List<Object[]> findCategoryAttributesRaw(Long categoryId);

}
