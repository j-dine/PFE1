package com.example.workflow.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class InternalServiceTokenProvider {

    private static final Duration TOKEN_TTL = Duration.ofHours(12);

    @Value("${jwt.secret}")
    private String secret;

    private volatile String cachedToken;
    private volatile Instant cachedExp;

    public String getToken() {
        Instant now = Instant.now();
        String token = cachedToken;
        Instant exp = cachedExp;
        if (token != null && exp != null && exp.isAfter(now.plusSeconds(30))) {
            return token;
        }
        synchronized (this) {
            now = Instant.now();
            token = cachedToken;
            exp = cachedExp;
            if (token != null && exp != null && exp.isAfter(now.plusSeconds(30))) {
                return token;
            }
            cachedToken = generateToken(now);
            cachedExp = now.plus(TOKEN_TTL);
            return cachedToken;
        }
    }

    private String generateToken(Instant now) {
        Date issuedAt = Date.from(now);
        Date expiresAt = Date.from(now.plus(TOKEN_TTL));
        return Jwts.builder()
                .setSubject("workflow-service")
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                // Use admin role so internal calls can update statut/create paiement/notifications.
                .claim("roles", List.of("ROLE_ADMIN"))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}

