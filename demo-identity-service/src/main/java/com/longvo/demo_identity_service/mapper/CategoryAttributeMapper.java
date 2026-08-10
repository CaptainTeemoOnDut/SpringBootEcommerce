package com.longvo.demo_identity_service.mapper;

import com.longvo.demo_identity_service.dto.response.AttributeResponse;
import com.longvo.demo_identity_service.entity.CategoryAttribute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CategoryAttributeMapper {

    @Mappings({
            @Mapping(target = "code", source = "attribute.code"),
            @Mapping(target = "name", source = "attribute.name"),
            @Mapping(target = "type", source = "attribute.dataType"),
            @Mapping(target = "unit", source = "attribute.unit"),
            @Mapping(target = "required", source = "required")
    })
    AttributeResponse toCategoryAttributeResponse(CategoryAttribute categoryAttribute);
}

