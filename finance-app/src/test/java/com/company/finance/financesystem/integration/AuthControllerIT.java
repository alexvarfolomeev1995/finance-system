package com.company.finance.financesystem.integration;

import com.company.finance.financesystem.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;
//НЕ ДОДЕЛАНО, НЕ РАБОТАЕТ
class AuthControllerIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void register_shouldCreateUser() {
        String json = """
            {"email":"it-test@example.com","password":"password123","fullName":"IT Test"}
            """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/auth/register", new HttpEntity<>(json, headers), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void login_shouldReturnTokens() {
        String json = """
            {"email":"admin@finance.local","password":"admin123"}
            """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/auth/login", new HttpEntity<>(json, headers), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("accessToken");
        assertThat(response.getBody()).contains("refreshToken");
    }
}