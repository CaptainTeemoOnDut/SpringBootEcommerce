package com.longvo.demo_identity_service.entity.interfaces;

import com.longvo.demo_identity_service.enums.AttributeType;

public interface CategoryAttributeProjection {

    Long getAttributeId();

    String getCode();

    String getName();

    String getDataType();

    String getUnit();

    Boolean getRequired();

    Long getCategoryId();
}

