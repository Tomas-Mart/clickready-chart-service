package com.clickready.chart.presentation.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO для запроса на создание/обновление данных графика.
 *
 * <p>Использует Record для неизменяемости и автоматической генерации методов.
 *
 * @param date        дата, за которую сохраняются данные
 * @param cost        затраты в USD (неотрицательные)
 * @param cpa         Cost Per Acquisition (неотрицательный)
 * @param roi         Return on Investment в процентах
 * @param conversions количество конверсий (неотрицательное)
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
@Schema(description = "Запрос на создание/обновление данных графика")
public record ChartDataRequest(

        @NotNull(message = "Дата не может быть пустой")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(
                description = "Дата",
                example = "2026-06-13",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        LocalDate date,

        @PositiveOrZero(message = "Затраты не могут быть отрицательными")
        @Schema(
                description = "Затраты в USD",
                example = "55.65",
                minimum = "0"
        )
        double cost,

        @PositiveOrZero(message = "CPA не может быть отрицательным")
        @Schema(
                description = "Cost Per Acquisition",
                example = "0.79",
                minimum = "0"
        )
        double cpa,

        @Schema(
                description = "Return on Investment в процентах",
                example = "56.33"
        )
        double roi,

        @PositiveOrZero(message = "Конверсии не могут быть отрицательными")
        @Schema(
                description = "Количество конверсий",
                example = "70",
                minimum = "0"
        )
        int conversions
) {
}