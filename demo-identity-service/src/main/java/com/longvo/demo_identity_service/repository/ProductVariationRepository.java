package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.dto.response.ProductVariationFlatRow;
import com.longvo.demo_identity_service.entity.ProductVariation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariationRepository extends JpaRepository<ProductVariation, Long> {
    //Page<Order> findByUserEmail(@Param("email") String email, Pageable pageable);

    @Query("""
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
        List<ProductVariationFlatRow> findFullVariations(Long productId);



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
