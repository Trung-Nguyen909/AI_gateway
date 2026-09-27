package com.baotrung.ai_gateway.service;

import com.baotrung.ai_gateway.entity.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class JwtService {

    private static final Duration TOKEN_LIFETIME = Duration.ofHours(1);

    private final JwtEncoder jwtEncoder;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateAccessToken(User user) {

        Instant now = Instant.now();
        Instant expiresAt = now.plus(TOKEN_LIFETIME);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("ai-gateway")
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .build();

        Jwt jwt = jwtEncoder.encode(
                JwtEncoderParameters.from(claims)
        );

        return jwt.getTokenValue();
    }
}