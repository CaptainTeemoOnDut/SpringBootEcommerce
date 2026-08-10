package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.dto.response.CartItemResponse;
import com.longvo.demo_identity_service.dto.response.ProductVariationFlatRow;
import com.longvo.demo_identity_service.entity.ProductVariant;
import com.longvo.demo_identity_service.entity.ProductVariation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    @Query("SELECT v FROM ProductVariant v " +
            "LEFT JOIN FETCH v.product p " +
            "LEFT JOIN FETCH p.shop " +
            "WHERE v.id = :id")
    ProductVariant findByCartItemId(@Param("id") Long id);

    @Query("SELECT v FROM ProductVariant v " +
            "LEFT JOIN FETCH v.product p " +
            "LEFT JOIN FETCH p.shop " +
            "WHERE v.id IN (:variantIds)")
    List<ProductVariant> findAllWithProductAndShopByVariantIds(@Param("variantIds") List<Long> variantIds);

    @Query("SELECT v FROM ProductVariant v " +
            "LEFT JOIN FETCH v.product " +
            "LEFT JOIN FETCH v.attributes attr " +
            "LEFT JOIN FETCH attr.option opt " +
            "LEFT JOIN FETCH opt.medias " +
            "WHERE v.id = :id")
    Optional<ProductVariant> findByIdWithDetails(@Param("id") Long id);

    @Query("""
    select new com.longvo.demo_identity_service.dto.response.CartItemResponse(
        v.id,
        v.product.name,
        v.imageUrl, 
        v.price,     
        0,           
        v.sku,
        v.product.shop.name,
        v.product.shop.id
    )
    from ProductVariant v
    join v.product p
    where v.id in (:variantIds)
""")
    List<CartItemResponse> findProductsByVariantIds(@Param("variantIds") List<Long> variantIds);
    //Page<Order> findByUserEmail(@Param("email") String email, Pageable pageable);

    /*@Query("""
    select new com.longvo.demo_identity_service.dto.response.ProductVariationFlatRow(
        v.id,
        v.code,
        v.name,
        o.id,
        o.value,
        m.publicUrl
    )
    from ProductVariation v
    join v.options o
    left join o.medias m
    where v.product.id = :productId
    order by v.position, o.position, m.position
    """)
        List<ProductVariationFlatRow> findFullVariations(Long productId);*/



    /*@Query(
            value = """
        select new com.longvo.demo_identity_service.dto.response.ProductVariantResponse(
            pv.id,
            pv.price,
            pv.stock,
            pv.imageUrl
            
        )
        from ProductVariant pv
        
        where pv.product.id = :productId
     """
    )
    List<ProductVariantResponse> getSKU(
            @Param("productId") Long productId
    );

    @Query(
            value = """
        select new com.longvo.demo_identity_service.dto.response.VariantOptionResponse(
            vo.id,
            vo.name
            
        )
        from VariantOption vo
        
        where vo.product.id = :productId
        order by vo.position asc
     """
    )
    List<VariantOptionValueResponse> getVariantOptions(
            @Param("productId") Long productId
    );*/



}
