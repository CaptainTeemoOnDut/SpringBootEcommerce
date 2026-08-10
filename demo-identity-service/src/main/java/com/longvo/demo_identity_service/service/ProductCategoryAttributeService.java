package com.longvo.demo_identity_service.service;

import com.longvo.demo_identity_service.entity.interfaces.CategoryAttributeProjection;
import com.longvo.demo_identity_service.repository.CategoryAttributeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductCategoryAttributeService {

    private final CategoryAttributeRepository repository;

    public List<CategoryAttributeProjection> getAttributesForCategory(Long categoryId) {
        return repository.findAllAttributesByCategoryTree(categoryId);
    }

}

