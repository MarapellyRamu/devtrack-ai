package com.devtrack.security;

import java.util.Map;

import com.devtrack.repository.UserRepository;
import com.devtrack.service.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "debug=false",
        "logging.level.root=OFF",
        "logging.level.org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor=OFF",
        "logging.level.org.springframework.web.servlet.mvc.method.annotation.HttpEntityMethodProcessor=OFF",
        "logging.level.org.springframework.web.client.RestTemplate=OFF"
})
@ActiveProfiles("test")
class JwtHttpAuthenticationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void tokenIssuedByLoginIsAcceptedByProtectedRequestInEmbeddedServer() {
        String baseUrl = "http://localhost:" + port;
        ResponseEntity<JsonNode> registration = restTemplate.postForEntity(baseUrl + "/api/auth/register",
                Map.of("name", "JWT Test User", "email", "jwt-http-test@example.com",
                        "password", "test-password-42"), JsonNode.class);
        assertThat(registration.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<JsonNode> login = restTemplate.postForEntity(baseUrl + "/api/auth/login",
                Map.of("email", "jwt-http-test@example.com", "password", "test-password-42"), JsonNode.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = login.getBody().path("accessToken").asText();
        assertThat(token).isNotBlank();
        assertThat(jwtService.extractSubject(token)).isEqualTo("jwt-http-test@example.com");

        String protectedUrl = baseUrl + "/actuator/info";
        ResponseEntity<String> withoutToken = restTemplate.getForEntity(protectedUrl, String.class);
        assertThat(withoutToken.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> withLoginToken = restTemplate.exchange(protectedUrl, HttpMethod.GET,
                new HttpEntity<>(headers), String.class);

        assertThat(withLoginToken.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> projects = restTemplate.exchange(baseUrl + "/api/projects",
                HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(projects.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
