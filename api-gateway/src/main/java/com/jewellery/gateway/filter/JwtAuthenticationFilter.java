package com.jewellery.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.Date;
import java.util.List;

/**
 * Global JWT authentication filter for API Gateway
 * Validates JWT tokens and allows/blocks requests accordingly
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    // Paths that don't require authentication
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html",
            "/webjars/",
            "/actuator",
            // Service-specific Swagger paths for aggregation
            "/user-service/v3/api-docs",
            "/product-service/v3/api-docs",
            "/inventory-service/v3/api-docs",
            "/order-service/v3/api-docs",
            "/cart-service/v3/api-docs",
            "/payment-service/v3/api-docs",
            "/shipping-service/v3/api-docs",
            "/notification-service/v3/api-docs",
            "/store-service/v3/api-docs",
            "/review-service/v3/api-docs");

    // Paths with public GET access
    private static final List<String> PUBLIC_GET_PATHS = List.of(
            "/api/v1/products",
            "/api/v1/categories",
            "/api/v1/collections",
            "/api/v1/attribute-types",
            "/api/v1/attribute-values",
            "/api/v1/stores",
            "/api/v1/reviews",
            "/api/v1/product-images");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        // Check if path is public
        if (isPublicPath(path) || (method.equals("GET") && isPublicGetPath(path))) {
            return chain.filter(exchange);
        }

        // Check for Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            // Validate token
            Claims claims = extractAllClaims(token);

            // Check if token is expired
            if (claims.getExpiration().before(new Date())) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Add user info to headers for downstream services
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Email", claims.getSubject())
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isPublicGetPath(String path) {
        return PUBLIC_GET_PATHS.stream().anyMatch(path::startsWith);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public int getOrder() {
        return -100; // Execute before other filters
    }
}
