package com.clickready.chart.security;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.clickready.chart.infrastructure.security.JwtTokenProvider;
import com.clickready.chart.integration.TestcontainersConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * Интеграционные тесты безопасности.
 */
@Slf4j
@Testcontainers
@Import(TestcontainersConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Интеграционные тесты безопасности")
class SecurityTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String validToken;
    private String expiredToken;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Testcontainers настройки
    }

    @BeforeEach
    void setUp() {
        log.info("Подготовка тестов безопасности на порту: {}", port);
        RestAssured.port = port;

        validToken = jwtTokenProvider.generateToken("testuser", List.of("ROLE_USER"));
        log.debug("Сгенерирован валидный токен для тестов");

        expiredToken = jwtTokenProvider.generateToken("testuser", List.of("ROLE_USER"));
    }

    // ============================================================
    // ТЕСТЫ
    // ============================================================

    @Test
    @DisplayName("Публичные эндпоинты доступны без токена")
    void shouldAllowPublicEndpointsWithoutToken() {
        log.info("=== Тест: публичные эндпоинты ===");

        // Health check
        given()
                .when()
                .get("/api/v1/chart/health")
                .then()
                .statusCode(200)
                .body(equalTo("Service is healthy"));
        log.info("✅ Health check доступен без токена");

        // Actuator health
        given()
                .when()
                .get("/actuator/health")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
        log.info("✅ Actuator health доступен без токена");
    }

    @Test
    @DisplayName("Защищенные эндпоинты возвращают 401 без токена")
    void shouldReturn401WithoutToken() {
        log.info("=== Тест: защищенные эндпоинты без токена ===");

        // GET /data
        given()
                .when()
                .get("/api/v1/chart/data")
                .then()
                .statusCode(401);
        log.info("✅ GET /data требует аутентификации");

        // POST /data
        given()
                .contentType(ContentType.JSON)
                .body("{\"date\":\"2026-06-13\",\"cost\":55.65,\"cpa\":0.79,\"roi\":56.33,\"conversions\":70}")
                .when()
                .post("/api/v1/chart/data")
                .then()
                .statusCode(401);
        log.info("✅ POST /data требует аутентификации");

        // DELETE /data/{date}
        given()
                .when()
                .delete("/api/v1/chart/data/2026-06-13")
                .then()
                .statusCode(401);
        log.info("✅ DELETE /data требует аутентификации");
    }

    @Test
    @DisplayName("Валидный JWT токен дает доступ к защищенным эндпоинтам")
    void shouldAllowAccessWithValidToken() {
        log.info("=== Тест: доступ с валидным токеном ===");

        // GET /data с токеном
        given()
                .header("Authorization", "Bearer " + validToken)
                .when()
                .get("/api/v1/chart/data")
                .then()
                .statusCode(200);
        log.info("✅ GET /data доступен с валидным токеном");

        // POST /data с токеном
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + validToken)
                .body("{\"date\":\"2026-06-13\",\"cost\":55.65,\"cpa\":0.79,\"roi\":56.33,\"conversions\":70}")
                .when()
                .post("/api/v1/chart/data")
                .then()
                .statusCode(201);
        log.info("✅ POST /data доступен с валидным токеном");
    }

    @Test
    @DisplayName("Невалидный JWT токен возвращает 401")
    void shouldReturn401WithInvalidToken() {
        log.info("=== Тест: доступ с невалидным токеном ===");

        // Невалидный токен
        given()
                .header("Authorization", "Bearer invalid_token_12345")
                .when()
                .get("/api/v1/chart/data")
                .then()
                .statusCode(401);  // ✅ Исправлено: ожидаем 401
        log.info("✅ Невалидный токен возвращает 401");

        // Неправильный префикс
        given()
                .header("Authorization", "Token " + validToken)
                .when()
                .get("/api/v1/chart/data")
                .then()
                .statusCode(401);  // ✅ Исправлено: ожидаем 401
        log.info("✅ Неправильный префикс возвращает 401");

        // Пустой токен
        given()
                .header("Authorization", "Bearer ")
                .when()
                .get("/api/v1/chart/data")
                .then()
                .statusCode(401);  // ✅ Исправлено: ожидаем 401
        log.info("✅ Пустой токен возвращает 401");
    }

    @Test
    @DisplayName("CORS заголовки настроены корректно")
    void shouldReturnCorrectCorsHeaders() {
        log.info("=== Тест: CORS заголовки ===");

        given()
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "Authorization,Content-Type")
                .when()
                .options("/api/v1/chart/data")
                .then()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", "http://localhost:3000")
                .header("Access-Control-Allow-Methods", containsString("GET"))
                .header("Access-Control-Allow-Methods", containsString("POST"))
                .header("Access-Control-Allow-Methods", containsString("DELETE"))
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Max-Age", "3600");

        log.info("✅ CORS заголовки настроены корректно");
    }

    @Test
    @DisplayName("Запросы с запрещенного origin блокируются")
    void shouldBlockRequestsFromForbiddenOrigin() {
        log.info("=== Тест: запрещенный origin ===");

        given()
                .header("Origin", "http://malicious-site.com")
                .when()
                .options("/api/v1/chart/data")
                .then()
                .header("Access-Control-Allow-Origin", not("http://malicious-site.com"));

        log.info("✅ Запросы с запрещенного origin блокируются");
    }

    @Test
    @DisplayName("JWT токен содержит правильные данные")
    void shouldContainCorrectDataInJwt() {
        log.info("=== Тест: содержимое JWT токена ===");

        var username = jwtTokenProvider.getUsernameFromToken(validToken);
        var roles = jwtTokenProvider.getRolesFromToken(validToken);

        assertThat(username).isEqualTo("testuser");
        assertThat(roles).contains("ROLE_USER");

        log.info("✅ JWT токен содержит правильные данные: username={}, roles={}", username, roles);
    }

    @Test
    @DisplayName("JWT токен валидируется корректно")
    void shouldValidateJwtTokenCorrectly() {
        log.info("=== Тест: валидация JWT токена ===");

        var isValid = jwtTokenProvider.validateToken(validToken);
        var isInvalid = jwtTokenProvider.validateToken("invalid_token");

        assertThat(isValid).isTrue();
        assertThat(isInvalid).isFalse();

        log.info("✅ Валидный токен: {}, невалидный: {}", isValid, isInvalid);
    }

    @Test
    @DisplayName("Аутентификация через JWT работает")
    void shouldAuthenticateViaJwt() {
        log.info("=== Тест: аутентификация через JWT ===");

        var authentication = jwtTokenProvider.getAuthentication(validToken);

        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("testuser");
        assertThat(authentication.isAuthenticated()).isTrue();

        log.info("✅ Аутентификация через JWT работает");
    }
}