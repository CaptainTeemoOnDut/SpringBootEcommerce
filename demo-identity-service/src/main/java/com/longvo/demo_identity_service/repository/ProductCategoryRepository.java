package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.dto.response.CategoryHomeResponse;
import com.longvo.demo_identity_service.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    @Query("""
    select new com.longvo.demo_identity_service.dto.response.CategoryHomeResponse(
        p.id,
        p.name,
        p.imageUrl
    )
    from ProductCategory p
    where p.parent.id is null 
""")
    List<CategoryHomeResponse> getProductCategoriesForHome();



}
