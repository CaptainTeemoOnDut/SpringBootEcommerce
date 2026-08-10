package com.longvo.demo_identity_service.repository;

import com.longvo.demo_identity_service.dto.request.ProductSearchRequest;
import com.longvo.demo_identity_service.dto.response.ProductCardResponse;
import com.longvo.demo_identity_service.entity.QProduct;
import com.longvo.demo_identity_service.entity.QProductCategory;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public ProductRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<ProductCardResponse> searchProducts(ProductSearchRequest request, Pageable pageable) {

        QProduct product = QProduct.product;
        QProductCategory category = QProductCategory.productCategory;

        List<ProductCardResponse> content = queryFactory
                .select(Projections.constructor(
                        ProductCardResponse.class,
                        product.id,
                        product.name,
                        product.thumbnailUrl,
                        product.unitPrice,
                        product.soldCount
                ))
                .from(product)
                .leftJoin(product.category, category)
                .where(
                        hasKeyword(request.getKeyword()),
                        hasCategory(request.getCategoryId()),
                        hasPriceBetween(request.getMinPrice(), request.getMaxPrice()),
                        hasMinRating(request.getMinRating())   // ⭐ NEW
                )
                .orderBy(getSort(request.getSort(), request.getOrder(), product))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(product.count())
                .from(product)
                .where(
                        hasKeyword(request.getKeyword()),
                        hasCategory(request.getCategoryId()),
                        hasPriceBetween(request.getMinPrice(), request.getMaxPrice()),
                        hasMinRating(request.getMinRating())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    private OrderSpecifier<?> getSort(String sort, String order, QProduct product) {

        Order direction = "asc".equalsIgnoreCase(order)
                ? Order.ASC
                : Order.DESC;

        if (sort == null) {
            return new OrderSpecifier<>(Order.DESC, product.createdAt); // default
        }

        return switch (sort) {
            case "price" -> new OrderSpecifier<>(direction, product.unitPrice);
            case "new" -> new OrderSpecifier<>(Order.DESC, product.createdAt);
            case "sales" -> new OrderSpecifier<>(Order.DESC, product.soldCount);
            default -> new OrderSpecifier<>(Order.DESC, product.createdAt);
        };
    }

    private OrderSpecifier<?> getOrder(String order, QProduct product) {

        if (order == null) {
            return product.unitPrice.desc(); // default = newest
        }

        return switch (order) {
            case "asc" -> product.unitPrice.asc();
            case "desc" -> product.unitPrice.desc();

            default -> product.unitPrice.desc();
        };
    }

    private BooleanExpression hasMinRating(Double minRating) {
        return minRating != null
                ? QProduct.product.avgRating.goe(minRating)
                : null;
    }

    private BooleanExpression hasKeyword(String keyword) {
        return keyword != null
                ? QProduct.product.name.containsIgnoreCase(keyword)
                : null;
    }

    private BooleanExpression hasCategory(Long categoryId) {
        return categoryId != null
                ? QProduct.product.category.id.eq(categoryId)
                : null;
    }

    private BooleanExpression hasPriceBetween(BigDecimal min, BigDecimal max) {
        if (min != null && max != null) {
            return QProduct.product.unitPrice.between(min, max);
        }
        return null;
    }

    /*@Override
    public Page<ProductCardResponse> searchProducts(ProductSearchRequest request, Pageable pageable) {

        QProduct product = QProduct.product;
        //QCategory category = QCategory.category;

        BooleanBuilder builder = new BooleanBuilder();

        if (request.getKeyword() != null) {
            builder.and(product.name.containsIgnoreCase(request.getKeyword()));
        }

        List<ProductCardResponse> content = queryFactory
                .select(Projections.constructor(
                        ProductCardResponse.class,
                        product.id,
                        product.name,
                        product.thumbnailUrl,
                        product.unitPrice,
                        product.soldCount

                ))
                .from(product)
                //.leftJoin(product.category, category)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(product.count())
                .from(product)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }*/
}
