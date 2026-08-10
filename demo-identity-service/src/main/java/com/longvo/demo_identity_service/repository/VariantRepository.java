package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.dto.response.VariantInforDTO;
import com.longvo.demo_identity_service.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariantRepository extends JpaRepository<ProductVariant, Long> {
    //Page<Order> findByUserEmail(@Param("email") String email, Pageable pageable);


    /*@Query("""
select new com.longvo.demo_identity_service.dto.response.ProductVariantFlatRow(
    v.id,
    var.code,
    opt.id,
    v.price,
    v.stock,
    v.sku
)
from ProductVariant v
join v.attributes a
join a.variation var
join a.option opt
where v.product.id = :productId
order by v.id
""")
    List<ProductVariantFlatRow> findVariantLookup(
            @Param("productId") Long productId
    );*/



    @Query("SELECT DISTINCT v FROM ProductVariant v " +
            "LEFT JOIN FETCH v.attributes a " +
            "LEFT JOIN FETCH a.option o " +
            "WHERE v.product.id = :productId")
    List<ProductVariant> findAllByProductId(@Param("productId") Long productId);

    @Query("""
        SELECT new com.longvo.demo_identity_service.dto.response.VariantInforDTO(
            v.id,
            v.price,
            v.stock
        )
        FROM ProductVariant v
        WHERE v.id = :variantId
    """)
    Optional<VariantInforDTO> findVariantInforById(@Param("variantId") Long variantId);


/*
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
