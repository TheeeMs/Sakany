package com.theMs.sakany.shared.auth;

import com.theMs.sakany.accounts.internal.domain.Role;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final JwtParser jwtParser;
    private final JwtConfig jwtConfig;

    public JwtService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;

        if (jwtConfig.getSecretKey() == null || jwtConfig.getSecretKey().isBlank()) {
            throw new IllegalStateException("auth.secret-key must be configured");
        }

        this.signingKey = Keys.hmacShaKeyFor(jwtConfig.getSecretKey().getBytes(StandardCharsets.UTF_8));
        this.jwtParser = Jwts.parser().verifyWith(signingKey).build();
    }

    public String generateAccessToken(UUID userId, Role role) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(jwtConfig.getAccessTokenExpiration());

        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role.name())
                .claim("tokenType", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(jwtConfig.getRefreshTokenExpiration());

        return Jwts.builder()
                .subject(userId.toString())
                .claim("tokenType", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    public AccessTokenPayload validateToken(String token) {
        try {
            Claims claims = jwtParser.parseSignedClaims(token).getPayload();

            UUID userId = UUID.fromString(claims.getSubject());
            String roleClaim = claims.get("role", String.class);
            Role role = roleClaim == null ? null : Role.valueOf(roleClaim);

            Instant issuedAt = claims.getIssuedAt().toInstant();
            Instant expiresAt = claims.getExpiration().toInstant();

            return new AccessTokenPayload(userId, role, issuedAt, expiresAt);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessRuleException("Invalid or expired token");
        }
    }

    public boolean isAccessToken(String token) {
        return "access".equals(getTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(getTokenType(token));
    }

    private String getTokenType(String token) {
        try {
            Claims claims = jwtParser.parseSignedClaims(token).getPayload();
            return claims.get("tokenType", String.class);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
