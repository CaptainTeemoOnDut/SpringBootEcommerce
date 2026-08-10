package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.entity.ProductVariationOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VariationOptionRepository extends JpaRepository<ProductVariationOption, Long> {
    //Page<Order> findByUserEmail(@Param("email") String email, Pageable pageable);


    /*@Query(
            value = """
        select new com.longvo.demo_identity_service.dto.response.ProductVariationOptionResponse(
            pvo.id,
            pvo.value
          
        )
            
        from ProductVariationOption pvo
        
        where pvo.variation.id = :variationId
     """
    )
    List<ProductVariationOptionResponse> getOptions(
            @Param("variationId") Long variationId
    );*/




}
