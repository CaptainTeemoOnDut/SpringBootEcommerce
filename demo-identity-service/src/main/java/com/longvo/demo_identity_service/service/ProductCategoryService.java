package com.longvo.demo_identity_service.service;

import com.longvo.demo_identity_service.dto.response.AttributeResponse;
import com.longvo.demo_identity_service.dto.response.CategoryHomeResponse;
import com.longvo.demo_identity_service.dto.response.CategoryTreeResponse;
import com.longvo.demo_identity_service.entity.CategoryAttribute;
import com.longvo.demo_identity_service.entity.ProductCategory;
import com.longvo.demo_identity_service.exception.AppException;
import com.longvo.demo_identity_service.exception.ErrorCode;
import com.longvo.demo_identity_service.mapper.CategoryAttributeMapper;
import com.longvo.demo_identity_service.mapper.CategoryHomeMapper;
import com.longvo.demo_identity_service.repository.CategoryAttributeRepository;
import com.longvo.demo_identity_service.repository.ProductCategoryRepository;
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
public class ProductCategoryService {

    ProductCategoryRepository productCategoryRepository;
    CategoryHomeMapper categoryHomeMapper;

    public List<CategoryTreeResponse> getProductCategoryTree() {

        List<ProductCategory> categories =
                productCategoryRepository.findAll();

        System.out.println("CATEGORIES:" + categories);
        System.out.println("SIZE = " + categories.size());


        Map<Long, CategoryTreeResponse> map = new HashMap<>();

        for (ProductCategory c : categories) {
            CategoryTreeResponse dto = new CategoryTreeResponse();
            dto.setId(c.getId());
            dto.setCategoryName(c.getName());
            map.put(c.getId(), dto);
        }

        List<CategoryTreeResponse> roots = new ArrayList<>();

        for (ProductCategory c : categories) {

            CategoryTreeResponse current = map.get(c.getId());

            if (c.getParent() == null) {
                roots.add(current);
            } else {
                CategoryTreeResponse parent =
                        map.get(c.getParent().getId());

                if (parent != null) {
                    parent.getChildren().add(current);
                } else {
                    log.warn("Orphan category id={}", c.getId());
                }
            }
        }

        roots.sort(Comparator.comparing(CategoryTreeResponse::getCategoryName));

        return roots;
    }

    /*public List<AttributeResponse> getCategoryAttributesByCategoryId(Long id) {

        if (!productCategoryRepository.existsById(id)) {
            throw new AppException(ErrorCode.PRODUCT_CATEGORY_NOT_EXISTED);
        }

        List<CategoryAttribute> categoryAttributes =
                categoryAttributeRepo.findByCategoryId(id);

        return categoryAttributes.stream()
                .map(categoryAttributeMapper::toCategoryAttributeResponse)
                .toList();

    }*/

    public List<CategoryHomeResponse> getCategoriesForHome() {
        //List<CategoryHomeResponse> categories = productCategoryRepository.getProductCategoriesForHome();

        return productCategoryRepository.getProductCategoriesForHome();
    }

}

