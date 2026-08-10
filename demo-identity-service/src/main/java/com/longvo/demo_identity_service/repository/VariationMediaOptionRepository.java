package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.entity.ProductVariationOptionMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VariationMediaOptionRepository extends JpaRepository<ProductVariationOptionMedia, Long> {
    //Page<Order> findByUserEmail(@Param("email") String email, Pageable pageable);

    /*@Query(
            value = """
        select new com.longvo.demo_identity_service.dto.response.ProductVariationOptionMediaResponse(
            pvom.publicUrl
          
        )
            
        from ProductVariationOptionMedia pvom
        
        where pvom.productVariationOption.id = :variationOptionId
     """
    )
    List<ProductVariationOptionMediaResponse> getOptions(
            @Param("variationOptionId") Long variationOptionId
    );*/





}
