package com.longvo.demo_identity_service.entity;

import com.longvo.demo_identity_service.enums.AddressType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    String recipientName; // Tên người nhận
    String phoneNumber;   // Số điện thoại nhận hàng

    String province;      // Tỉnh/Thành phố
    String district;      // Quận/Huyện
    String ward;          // Phường/Xã
    String streetDetail;  // Số nhà, tên đường

    boolean isDefault;    // Địa chỉ mặc định

    @Enumerated(EnumType.STRING)
    AddressType type;     // HOME, OFFICE

    // Thêm các trường audit nếu cần (CreatedBy, UpdatedAt...)


    public String getFullAddress() {
        return Stream.of(streetDetail, ward, district, province)
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(", "));
    }
}
