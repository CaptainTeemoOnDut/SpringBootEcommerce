package com.longvo.demo_identity_service.service;

import com.longvo.demo_identity_service.entity.RefreshToken;
import com.longvo.demo_identity_service.entity.User;
import com.longvo.demo_identity_service.repository.RefreshTokenRepository;
import com.longvo.demo_identity_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    private final long refreshTokenDurationMs = 30 * 24 * 60 * 60 * 1000L; // 30 ngày 30 * 24 * 60 * 60 * 1000L

    public RefreshToken createRefreshToken(User user) {
        return RefreshToken.builder()
                .user(user)
                .expiryTime(Date.from(Instant.now().plusMillis(refreshTokenDurationMs)))
                .token(UUID.randomUUID().toString())
                .build();

    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }


    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryTime().before(Date.from(Instant.now()))) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token đã hết hạn. Vui lòng đăng nhập lại.");
        }
        return token;
    }
}

