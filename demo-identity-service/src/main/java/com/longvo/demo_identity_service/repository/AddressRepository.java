package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.entity.Address;
import com.longvo.demo_identity_service.entity.OrderGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    Address findDefaultByUserId(Long userId);
}
