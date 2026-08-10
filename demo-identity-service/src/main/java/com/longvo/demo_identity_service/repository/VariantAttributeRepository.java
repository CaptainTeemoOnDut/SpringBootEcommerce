package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.entity.VariantAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VariantAttributeRepository extends JpaRepository<VariantAttribute, Long> {
    //Page<Order> findByUserEmail(@Param("email") String email, Pageable pageable);




    /*@Query(
            value = """
        select new com.longvo.demo_identity_service.dto.response.ProductVariantAttributeResponse(
            va.variation.code,
            va.option.id
            
        )
        from VariantAttribute va
        
        where va.variant.id = :variantId
        
     """
    )
    List<ProductVariantAttributeResponse> findAttributesByVariantId(
            @Param("variantId") Long variantId
    );*/


}
