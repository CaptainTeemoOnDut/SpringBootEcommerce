package com.longvo.demo_identity_service.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private final String secret = "replace_with_strong_secret_and_put_in_env";
    private final long validityMs = 1000L * 60 * 60 * 24; // 1 day

    public String generateToken(Object user) {
        // user can be any object — in real app use User id, roles, etc.
        return Jwts.builder()
                .setSubject(String.valueOf(user)) // adapt to your user model
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validityMs))
                .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                .compact();
    }
}
