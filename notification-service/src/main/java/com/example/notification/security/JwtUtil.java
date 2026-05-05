package com.example.notification.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private Key signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public List<String> extractRoles(String token) {
        Claims claims = parseClaims(token);
        Object raw = claims.get("roles");
        if (raw instanceof List<?> list) {
            return list.stream()
                    .map(v -> v == null ? "" : String.valueOf(v))
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toList());
        }
        if (raw instanceof String s && !s.isBlank()) {
            return List.of(s);
        }
        return Collections.emptyList();
    }
}
