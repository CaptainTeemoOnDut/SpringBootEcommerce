package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.entity.Product;
import com.longvo.demo_identity_service.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface ShopRepository extends JpaRepository<Shop, Long> {

        boolean existsByUser_Id(Long id);


}
