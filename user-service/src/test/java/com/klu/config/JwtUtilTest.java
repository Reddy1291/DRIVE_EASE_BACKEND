package com.klu.config;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "DriveEaseJWTSecretKey2026ForSecureTokenGenerationAndValidation");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtUtil.generateToken(1L, "user@test.com", "CUSTOMER");
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testExtractAllClaims() {
        String token = jwtUtil.generateToken(42L, "manager@test.com", "ADMIN");
        Claims claims = jwtUtil.extractAllClaims(token);

        assertEquals("manager@test.com", claims.getSubject());
        assertEquals(42, ((Number) claims.get("userId")).longValue());
        assertEquals("ADMIN", claims.get("role"));
    }

    @Test
    void testInvalidToken() {
        String fakeToken = "eyJhbGciOiJIUzI1NiJ9.invalidpayload.invalidsignature";
        assertFalse(jwtUtil.validateToken(fakeToken));
    }
}
