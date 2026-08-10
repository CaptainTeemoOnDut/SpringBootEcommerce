package com.longvo.demo_identity_service.dto.request;

import com.longvo.demo_identity_service.enums.PaymentMethods;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlaceOrderRequest {

    String receiverName;

    String phoneNumber;

    // 1. Những món hàng user quyết định mua (từ Preview)
    List<Long> selectedSkuIds;

    // 2. Địa chỉ user đã chọn (Có thể thay đổi so với mặc định)
    Long addressId;

    // 3. Phương thức thanh toán (COD, Ví Momo, thẻ...)
    PaymentMethods paymentMethod;

    // 4. Voucher/Mã giảm giá (nếu có)
    //String couponCode;

    // 5. Ghi chú cho từng đơn hàng (tùy chọn)
    //Map<Long, String> shopNotes; // shopId -> note
}
