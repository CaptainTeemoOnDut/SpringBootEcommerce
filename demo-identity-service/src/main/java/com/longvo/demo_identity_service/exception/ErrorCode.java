package com.longvo.demo_identity_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    PRODUCT_EXISTED(1009, "Product existed", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_EXISTED(1010, "Product not existed", HttpStatus.NOT_FOUND),
    PRODUCT_CATEGORY_NOT_EXISTED(1011, "Product Category not existed", HttpStatus.NOT_FOUND),
    TOKEN_EXPIRED(1012, "Token expired", HttpStatus.UNAUTHORIZED),
    REVIEW_ALREADY_EXISTS(1013, "You have already reviewed this product in this order", HttpStatus.BAD_REQUEST),
    REVIEW_NOT_EXISTED(1014, "Reviewed not existed", HttpStatus.BAD_REQUEST),
    USER_NOT_ACTIVE(1015, "User not active", HttpStatus.NOT_FOUND),
    SHOP_NOT_EXISTED(1016, "Shop not existed", HttpStatus.NOT_FOUND),
    SHOP_EXISTED(1017, "Shop existed", HttpStatus.BAD_REQUEST),
    USER_ALREADY_HAS_SHOP(1018, "User already has shop", HttpStatus.BAD_REQUEST),
    ATTRIBUTE_REQUIRED(1019, "Missing required attribute", HttpStatus.BAD_REQUEST),
    INVALID_ATTRIBUTE(1020, "Invalid Attribute", HttpStatus.BAD_REQUEST),
    INVALID_ATTRIBUTE_TYPE(1020, "Invalid Attribute Type", HttpStatus.BAD_REQUEST),
    VARIANT_NOT_EXISTS(1021, "Variant not existed", HttpStatus.NOT_FOUND),
    INVALID_VARIANT_ATTRIBUTE(1022, "Invalid variant attribute error", HttpStatus.BAD_REQUEST),
    DUPLICATE_VARIANT(1023, "Duplicate variant", HttpStatus.BAD_REQUEST),
    INVALID_VARIATION(1024, "Variation not existed", HttpStatus.NOT_FOUND),
    OUT_OF_STOCK(1025, "out of stock", HttpStatus.BAD_REQUEST),
    CART_ITEM_NOT_FOUND(1026, "Cart item not existed", HttpStatus.NOT_FOUND),
    ALREADY_CHECKED_OUT(1025, "already checked out", HttpStatus.BAD_REQUEST),
    ADDRESS_NOT_EXISTED(1026, "address not existed", HttpStatus.NOT_FOUND),
    ORDER_NOT_EXISTED(1027, "order not existed", HttpStatus.NOT_FOUND),
    ORDER_ITEM_NOT_EXISTED(1028, "order item not existed", HttpStatus.NOT_FOUND),
    PRODUCT_RATING_NOT_EXISTED(1029, "product rating not existed", HttpStatus.NOT_FOUND),
    PASSWORD_EXISTED(1030, "Password existed", HttpStatus.BAD_REQUEST),
    WRONG_PASSWORD(1031, "Wrong Password", HttpStatus.BAD_REQUEST),
    PASSWORD_DUPLICATED(1032, "Password duplicated", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_EXISTED(1033, "Password not existed", HttpStatus.NOT_FOUND),
    INVALID_CREDENTIALS(1034, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    TOKEN_GENERATION_FAILED(1035, "Token generation failed", HttpStatus.EXPECTATION_FAILED),
    UNAUTHORIZED(1035, "Unauthorized", HttpStatus.UNAUTHORIZED),;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
