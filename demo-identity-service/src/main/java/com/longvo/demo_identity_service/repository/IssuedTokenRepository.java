package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.entity.InvalidatedToken;
import com.longvo.demo_identity_service.entity.IssuedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssuedTokenRepository extends JpaRepository<IssuedToken, String> {
}
