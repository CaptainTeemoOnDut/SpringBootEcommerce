package com.longvo.demo_identity_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String token; //UUID

    //Date expiryTime;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    boolean revoked;

    //@OneToMany(mappedBy = "refreshToken", cascade = CascadeType.ALL, orphanRemoval = true)
    //List<IssuedToken> issuedTokens = new ArrayList<>();
}

