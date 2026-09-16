package com.klu.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private static final String SECRET = "DriveEaseJWTSecretKey2026ForSecureTokenGenerationAndValidation";

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "secret", SECRET);
    }

    private String generateValidToken() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("john@example.com")
                .claim("userId", 100L)
                .claim("role", "CUSTOMER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }

    @Test
    void testPublicEndpointRegisterPasses() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/users").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean filterChainExecuted = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            filterChainExecuted.set(true);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();
        assertTrue(filterChainExecuted.get());
    }

    @Test
    void testPublicEndpointLoginPasses() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/users/login").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean filterChainExecuted = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            filterChainExecuted.set(true);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();
        assertTrue(filterChainExecuted.get());
    }

    @Test
    void testProtectedEndpointWithoutAuthHeaderReturns401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/vehicles").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean filterChainExecuted = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            filterChainExecuted.set(true);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();
        assertFalse(filterChainExecuted.get());
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void testProtectedEndpointWithInvalidTokenReturns401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/vehicles")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.jwt.token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean filterChainExecuted = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            filterChainExecuted.set(true);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();
        assertFalse(filterChainExecuted.get());
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void testProtectedEndpointWithValidTokenPassesAndAddsHeaders() {
        String token = generateValidToken();

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/vehicles")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean filterChainExecuted = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            filterChainExecuted.set(true);
            // Verify forwarded headers
            assertEquals("100", ex.getRequest().getHeaders().getFirst("X-User-Id"));
            assertEquals("john@example.com", ex.getRequest().getHeaders().getFirst("X-User-Email"));
            assertEquals("CUSTOMER", ex.getRequest().getHeaders().getFirst("X-User-Role"));
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();
        assertTrue(filterChainExecuted.get());
    }
}
