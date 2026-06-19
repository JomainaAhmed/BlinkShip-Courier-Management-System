package com.capg.gateway.filter;

import com.capg.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;

import org.springframework.cloud.gateway.filter.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class JwtFilter implements GlobalFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtUtil.validateToken(token);

            String username = claims.get("username") != null ? claims.get("username").toString() : claims.getSubject();
            Object roleObj = claims.get("role");
            
            if (roleObj == null) {
                return unauthorized(exchange, "Role claim missing");
            }
            
            String role = roleObj.toString();

            if (isAdminPath(path) && !role.equals("ADMIN")) {
                return forbidden(exchange, "Admin access required");
            }

            var mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User", username)
                    .header("X-Role", role)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            return unauthorized(exchange, "Invalid or expired token");
        }
    }

    private boolean isPublicEndpoint(String path) {
        return path.contains("/auth/signup") ||
               path.contains("/auth/login") ||
               path.contains("/v3/api-docs") ||
               path.contains("/swagger-ui") ||
               path.contains("/actuator") ||
               path.contains("/error");
    }

    private boolean isAdminPath(String path) {
        return path.contains("/admin");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }
}