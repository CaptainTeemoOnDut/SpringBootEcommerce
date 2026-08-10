package com.longvo.demo_identity_service.repository;


import com.longvo.demo_identity_service.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StateRepository extends JpaRepository<State,Integer> {

    List<State> findByCountryCode(String code);
}
