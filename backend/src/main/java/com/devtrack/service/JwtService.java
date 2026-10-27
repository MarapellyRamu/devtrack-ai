package com.devtrack.service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.devtrack.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(JwtProperties properties) {
        if (properties.secret() == null || properties.secret().isBlank()) {
            throw new IllegalStateException("JWT_SECRET must be set to a Base64-encoded secret of at least 32 bytes.");
        }

        byte[] secretBytes;
        try {
            secretBytes = Decoders.BASE64.decode(properties.secret());
        } catch (RuntimeException exception) {
            throw new IllegalStateException("JWT_SECRET must be valid Base64.", exception);
        }
        if (secretBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must decode to at least 32 bytes for HS256.");
        }
        if (properties.expiration() == null || properties.expiration().isZero() || properties.expiration().isNegative()) {
            throw new IllegalStateException("JWT_EXPIRATION must be a positive duration, such as PT1H.");
        }

        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
        this.expirationMillis = properties.expiration().toMillis();
    }

    public String createToken(User user) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + expirationMillis);
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(signingKey)
                .compact();
    }

    public String extractSubject(String token) throws JwtException {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public long getExpirationSeconds() {
        return expirationMillis / 1000;
    }
}
