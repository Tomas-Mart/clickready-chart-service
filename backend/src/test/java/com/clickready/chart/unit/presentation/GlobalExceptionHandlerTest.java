package com.clickready.chart.unit.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;
import com.clickready.chart.presentation.exception.BusinessException;
import com.clickready.chart.presentation.exception.DataNotFoundException;
import com.clickready.chart.presentation.exception.GlobalExceptionHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("Тесты глобального обработчика исключений")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Обработка DataNotFoundException - возвращает 404 с деталями")
    void shouldHandleDataNotFoundException() {
        // Arrange
        var ex = new DataNotFoundException(
                "ChartData",
                "date",
                "2026-06-13",
                "Данные за 2026-06-13 не найдены"
        );
        var request = mock(WebRequest.class);

        // Act
        var response = handler.handleDataNotFoundException(ex, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        var body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTitle()).isEqualTo("Ресурс не найден");
        assertThat(body.getDetail()).isEqualTo("Данные за 2026-06-13 не найдены");

        var properties = body.getProperties();
        assertThat(properties)
                .containsEntry("resource", "ChartData")
                .containsEntry("field", "date")
                .containsEntry("value", "2026-06-13")
                .containsKey("timestamp");
    }

    @Test
    @DisplayName("Обработка BusinessException - возвращает 409 с кодом ошибки")
    void shouldHandleBusinessException() {
        // Arrange
        var ex = new BusinessException("Бизнес-ошибка", "ERR_001");
        var request = mock(WebRequest.class);

        // Act
        var response = handler.handleBusinessException(ex, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        var body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTitle()).isEqualTo("Бизнес-ошибка");
        assertThat(body.getDetail()).isEqualTo("Бизнес-ошибка");
        assertThat(body.getProperties())
                .containsEntry("errorCode", "ERR_001")
                .containsKey("timestamp");
    }

    @Test
    @DisplayName("Обработка IllegalArgumentException - возвращает 400")
    void shouldHandleIllegalArgumentException() {
        // Arrange
        var ex = new IllegalArgumentException("Неверный аргумент");
        var request = mock(WebRequest.class);

        // Act
        var response = handler.handleIllegalArgumentException(ex, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        var body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTitle()).isEqualTo("Неверный аргумент");
        assertThat(body.getDetail()).isEqualTo("Неверный аргумент");
        assertThat(body.getProperties()).containsKey("timestamp");
    }

    @Test
    @DisplayName("Обработка RuntimeException - возвращает 500")
    void shouldHandleGenericException() {
        // Arrange
        var ex = new RuntimeException("Внутренняя ошибка сервера");
        var request = mock(WebRequest.class);

        // Act
        var response = handler.handleGenericException(ex, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        var body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTitle()).isEqualTo("Внутренняя ошибка сервера");
        assertThat(body.getDetail()).isEqualTo("Произошла непредвиденная ошибка. Попробуйте позже.");
        assertThat(body.getProperties()).containsKey("timestamp");
    }

    @Test
    @DisplayName("Обработка DataNotFoundException - проверка всех полей")
    void shouldHandleDataNotFoundExceptionWithAllFields() {
        // Arrange
        var ex = new DataNotFoundException(
                "User",
                "email",
                "test@example.com",
                "Пользователь с email test@example.com не найден"
        );
        var request = mock(WebRequest.class);

        // Act
        var response = handler.handleDataNotFoundException(ex, request);

        // Assert
        var body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getProperties())
                .containsEntry("resource", "User")
                .containsEntry("field", "email")
                .containsEntry("value", "test@example.com");
        assertThat(body.getDetail()).isEqualTo("Пользователь с email test@example.com не найден");
    }
}