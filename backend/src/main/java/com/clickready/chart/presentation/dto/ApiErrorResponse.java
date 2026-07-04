package com.clickready.chart.presentation.dto;

import java.time.Instant;
import java.util.Map;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO для ответа с ошибкой API.
 *
 * <p>Соответствует спецификации Problem Details (RFC 7807).
 * Использует Record для неизменяемости.
 *
 * @param status               HTTP статус ошибки
 * @param title                краткое описание ошибки
 * @param detail               детальное описание ошибки
 * @param path                 путь запроса, вызвавшего ошибку
 * @param timestamp            время возникновения ошибки
 * @param additionalProperties дополнительные поля с ошибками
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
@Schema(description = "Ответ с ошибкой API")
public record ApiErrorResponse(

        @Schema(
                description = "HTTP статус",
                example = "400",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        int status,

        @Schema(
                description = "Заголовок ошибки",
                example = "Ошибка валидации"
        )
        String title,

        @Schema(
                description = "Детали ошибки",
                example = "Проверьте корректность данных"
        )
        String detail,

        @Schema(
                description = "Путь к ошибке",
                example = "/api/v1/chart/data"
        )
        String path,

        @Schema(
                description = "Временная метка",
                example = "2026-07-01T12:00:00Z"
        )
        Instant timestamp,

        @Schema(
                description = "Дополнительные поля с ошибками",
                example = "{\"field\": \"cost\", \"error\": \"не может быть отрицательным\"}"
        )
        Map<String, Object> additionalProperties
) {

    /**
     * Создает ответ с ошибкой.
     *
     * @param status HTTP статус
     * @param title  заголовок ошибки
     * @param detail детали ошибки
     * @param path   путь запроса
     * @return готовый объект ApiErrorResponse
     */
    public static ApiErrorResponse of(
            int status,
            String title,
            String detail,
            String path
    ) {
        return new ApiErrorResponse(
                status,
                title,
                detail,
                path,
                Instant.now(),
                Map.of()
        );
    }

    /**
     * Создает ответ с ошибкой и дополнительными полями.
     *
     * @param status               HTTP статус
     * @param title                заголовок ошибки
     * @param detail               детали ошибки
     * @param path                 путь запроса
     * @param additionalProperties дополнительные поля
     * @return готовый объект ApiErrorResponse
     */
    public static ApiErrorResponse of(
            int status,
            String title,
            String detail,
            String path,
            Map<String, Object> additionalProperties
    ) {
        return new ApiErrorResponse(
                status,
                title,
                detail,
                path,
                Instant.now(),
                additionalProperties
        );
    }
}