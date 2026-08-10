package com.longvo.demo_identity_service.enums;

public enum ProductRejectReason {

    INVALID_CONTENT(
            "Nội dung đăng bán không hợp lệ"
    ),

    COUNTERFEIT_PRODUCT(
            "Đăng bán hàng giả/nhái"
    ),

    SPAM(
            "Spam"
    ),

    INAPPROPRIATE_IMAGE(
            "Hình ảnh không phù hợp"
    ),

    MISSING_INFORMATION(
            "Sản phẩm cần bổ sung thông tin"
    ),

    MALL_VIOLATION(
            "Vi phạm dành cho Shopee Mall"
    ),

    OTHER_VIOLATION(
            "Vi phạm cần cải thiện khác"
    ),

    PQR_VIOLATION(
            "Vi phạm chỉ số PQR"
    );

    private final String label;

    ProductRejectReason(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

