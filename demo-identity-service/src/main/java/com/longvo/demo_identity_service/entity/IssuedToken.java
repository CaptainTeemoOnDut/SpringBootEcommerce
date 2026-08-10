package com.longvo.demo_identity_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.engine.internal.Cascade;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class IssuedToken {
    @Id
    @Column(length = 36)
    String jti;

    @Column(nullable = false)
    String userId;

    @Column(nullable = false)
    Date expiryTime;

    @ManyToOne
    @JoinColumn(name = "refresh_token_id", nullable = false)
    RefreshToken refreshToken;
}
