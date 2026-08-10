package com.longvo.demo_identity_service.component;

import com.longvo.demo_identity_service.dto.request.ProductFilterRequest;
import com.longvo.demo_identity_service.dto.response.ProductCardResponse;
import com.longvo.demo_identity_service.entity.CategoryHierarchy;
import com.longvo.demo_identity_service.entity.Product;
import com.longvo.demo_identity_service.entity.ProductCategory;
import com.longvo.demo_identity_service.service.OrderExpirationRedisService;
import com.longvo.demo_identity_service.service.OrderService;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Component
@RequiredArgsConstructor
@Slf4j
public class ProductSpecification {

    public static Specification<Product> filter(ProductFilterRequest req) {
        return (root, query, cb) -> {

            // tránh lỗi count query
            if (query.getResultType() != Long.class) {
                root.fetch("shop", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            // price >= minPrice
            if (req.getMinPrice() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("unitPrice"), req.getMinPrice())
                );
            }

            // price <= maxPrice
            if (req.getMaxPrice() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("unitPrice"), req.getMaxPrice())
                );
            }
            //System.out.println("CATEGORY ID: " + req.getCategoryId());
            // category
            if (req.getCategoryId() != null) {
                //System.out.println("CATEGORY ID: " + req.getCategoryId());

                Subquery<Long> subquery = query.subquery(Long.class);
                Root<CategoryHierarchy> ch = subquery.from(CategoryHierarchy.class);

                subquery.select(ch.get("descendant").get("id"))
                        .where(cb.equal(ch.get("ancestor").get("id"), req.getCategoryId()));

                predicates.add(
                        root.get("category").get("id").in(subquery)
                );

            }

            // rating >= minRating
            if (req.getMinRating() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("avgRating"), req.getMinRating())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
