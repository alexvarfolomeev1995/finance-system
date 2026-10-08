package com.company.finance.financesystem.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails user;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties(
                "test-secret-key-minimum-32-characters-long-1234567890",
                900L,
                604800L
        );
        jwtService = new JwtService(props);
        user = User.withUsername("test@test.com").password("x").authorities("ROLE_USER").build();
    }

    @Test
    void generateAccessToken_shouldBeValid() {
        String token = jwtService.generateAccessToken(user);
        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("test@test.com");
        assertThat(jwtService.isAccessToken(token)).isTrue();
        assertThat(jwtService.isRefreshToken(token)).isFalse();
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void generateRefreshToken_shouldBeRefreshType() {
        String token = jwtService.generateRefreshToken(user);
        assertThat(jwtService.isRefreshToken(token)).isTrue();
        assertThat(jwtService.isAccessToken(token)).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalse_forWrongUser() {
        String token = jwtService.generateAccessToken(user);
        UserDetails other = User.withUsername("other@test.com").password("x").authorities("ROLE_USER").build();
        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }
}