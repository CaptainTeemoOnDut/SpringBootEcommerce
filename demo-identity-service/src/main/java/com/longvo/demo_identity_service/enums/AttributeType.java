package com.longvo.demo_identity_service.enums;

import com.longvo.demo_identity_service.exception.AppException;
import com.longvo.demo_identity_service.exception.ErrorCode;

public enum AttributeType {
    STRING,
    NUMBER,
    BOOLEAN;

    public static AttributeType from(String value) {
        try {
            return AttributeType.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_ATTRIBUTE_TYPE);
        }
    }
}

