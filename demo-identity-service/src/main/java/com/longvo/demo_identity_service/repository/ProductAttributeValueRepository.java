package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.entity.CategoryAttribute;
import com.longvo.demo_identity_service.entity.Country;
import com.longvo.demo_identity_service.entity.ProductAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue,Long> {

}
