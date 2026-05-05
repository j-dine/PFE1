package com.example.gateway.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/users/login",
            "/api/auth/login",
            "/actuator"
    );

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS || isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange.getResponse());
        }

        String token = authHeader.substring(7);
        Claims claims = parseClaims(token);
        if (claims == null) {
            return unauthorized(exchange.getResponse());
        }

        Set<String> roles = extractRoles(claims);
        if (!isAuthorized(path, exchange, roles)) {
            return forbidden(exchange.getResponse());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    private Set<String> extractRoles(Claims claims) {
        Object raw = claims.get("roles");
        if (raw instanceof List<?> list) {
            return list.stream().map(v -> String.valueOf(v)).collect(java.util.stream.Collectors.toSet());
        }
        if (raw instanceof String s && !s.isBlank()) {
            return Set.of(s);
        }
        return Collections.emptySet();
    }

    private boolean isAuthorized(String path, ServerWebExchange exchange, Set<String> roles) {
        HttpMethod method = exchange.getRequest().getMethod();

        // Admin endpoints
        if (path.startsWith("/api/admin")) {
            return roles.contains("ROLE_ADMIN");
        }

        // Users listing/deletion are admin-only (login/register/me already handled as public/authenticated elsewhere)
        if (path.startsWith("/api/users")
                && !path.startsWith("/api/users/login")
                && !path.startsWith("/api/users/register")
                && !path.startsWith("/api/users/me")) {
            return roles.contains("ROLE_ADMIN");
        }

        // Dossiers: enforce role access at the edge too (defense-in-depth).
        if (path.startsWith("/api/dossiers")) {
            // Read = any authenticated user.
            if (HttpMethod.GET.equals(method)) return true;

            // Create dossier
            if (HttpMethod.POST.equals(method) && "/api/dossiers".equals(path)) {
                return hasAnyRole(roles, "ROLE_ADMIN", "ROLE_AGENT_BUREAU_ORDRE");
            }

            // Assign/archive/expedier = BO/Admin
            if (HttpMethod.POST.equals(method)
                    && (path.endsWith("/assign") || path.endsWith("/archive") || path.endsWith("/expedier"))) {
                return hasAnyRole(roles, "ROLE_ADMIN", "ROLE_AGENT_BUREAU_ORDRE");
            }

            // Add comment = any authenticated
            if (HttpMethod.POST.equals(method) && path.endsWith("/comments")) {
                return true;
            }

            // Patch = BO/Service/Resp/Admin
            if (HttpMethod.PATCH.equals(method)) {
                return hasAnyRole(roles, "ROLE_ADMIN", "ROLE_AGENT_BUREAU_ORDRE", "ROLE_AGENT_SERVICE", "ROLE_RESPONSABLE");
            }

            // Update statut = any role participating in workflow
            if (HttpMethod.PUT.equals(method) && path.endsWith("/statut")) {
                return hasAnyRole(roles,
                        "ROLE_ADMIN",
                        "ROLE_AGENT_BUREAU_ORDRE",
                        "ROLE_AGENT_SERVICE",
                        "ROLE_RESPONSABLE",
                        "ROLE_AGENT_FINANCIER");
            }

            // Delete dossier = admin only
            if (HttpMethod.DELETE.equals(method)) {
                return roles.contains("ROLE_ADMIN");
            }

            // Default deny for unknown dossier routes.
            return false;
        }

        // Documents: upload/delete restricted to BO/Admin, read allowed.
        if (path.startsWith("/api/documents")) {
            if (HttpMethod.GET.equals(method)) return true;
            if (HttpMethod.POST.equals(method) || HttpMethod.DELETE.equals(method)) {
                return hasAnyRole(roles, "ROLE_ADMIN", "ROLE_AGENT_BUREAU_ORDRE");
            }
            return false;
        }

        // Paiements: financier/admin write, resp can read/create (current UI), others denied.
        if (path.startsWith("/api/paiements")) {
            if (HttpMethod.GET.equals(method) || HttpMethod.HEAD.equals(method)) {
                return hasAnyRole(roles, "ROLE_ADMIN", "ROLE_AGENT_FINANCIER", "ROLE_RESPONSABLE");
            }
            if (HttpMethod.POST.equals(method)) {
                return hasAnyRole(roles, "ROLE_ADMIN", "ROLE_AGENT_FINANCIER", "ROLE_RESPONSABLE");
            }
            if (HttpMethod.PATCH.equals(method) || HttpMethod.DELETE.equals(method)) {
                return hasAnyRole(roles, "ROLE_ADMIN", "ROLE_AGENT_FINANCIER");
            }
            return false;
        }

        // Notifications: read/mark-read for any authenticated; create/delete admin-only.
        if (path.startsWith("/api/notifications")) {
            if (HttpMethod.GET.equals(method) || HttpMethod.PATCH.equals(method)) return true;
            if (HttpMethod.POST.equals(method) || HttpMethod.DELETE.equals(method)) {
                return roles.contains("ROLE_ADMIN");
            }
            return false;
        }

        // Camunda tasks: prevent reading tasks for another candidate group
        if (path.equals("/api/workflow/tasks")) {
            String candidateGroup = exchange.getRequest().getQueryParams().getFirst("candidateGroup");
            if (candidateGroup == null || candidateGroup.isBlank()) {
                // If no group filter, allow only admin to avoid leaking other groups tasks.
                return roles.contains("ROLE_ADMIN");
            }
            if (roles.contains("ROLE_ADMIN")) return true;
            String expected = expectedCandidateGroupForRoles(roles);
            return expected != null && expected.equalsIgnoreCase(candidateGroup);
        }

        // Everything else: authenticated token is enough here; fine-grained checks are enforced by services or UI.
        return true;
    }

    private boolean hasAnyRole(Set<String> roles, String... any) {
        if (roles == null || roles.isEmpty()) return false;
        for (String r : any) {
            if (roles.contains(r)) return true;
        }
        return false;
    }

    private String expectedCandidateGroupForRoles(Set<String> roles) {
        if (roles == null) return null;
        if (roles.contains("ROLE_AGENT_BUREAU_ORDRE")) return "BO";
        if (roles.contains("ROLE_AGENT_SERVICE")) return "SERVICE";
        if (roles.contains("ROLE_RESPONSABLE")) return "RESPONSABLE";
        if (roles.contains("ROLE_AGENT_FINANCIER")) return "FINANCIER";
        return null;
    }

    private Mono<Void> unauthorized(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    private Mono<Void> forbidden(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }
}
