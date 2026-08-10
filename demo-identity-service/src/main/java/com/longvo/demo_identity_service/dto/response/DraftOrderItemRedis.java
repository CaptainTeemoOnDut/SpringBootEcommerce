package com.longvo.demo_identity_service.dto.response;

import com.longvo.demo_identity_service.enums.AttributeType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DraftOrderItemRedis {
    private Long id;
    private int quantity;

}
