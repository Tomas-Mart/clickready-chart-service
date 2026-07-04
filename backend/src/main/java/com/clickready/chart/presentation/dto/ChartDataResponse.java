package com.clickready.chart.presentation.dto;

import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO для ответа API с данными графика.
 *
 * <p>Использует Record для неизменяемости и автоматической генерации методов.
 *
 * @param date        дата, за которую представлены данные
 * @param cost        затраты в USD
 * @param cpa         Cost Per Acquisition
 * @param roi         Return on Investment в процентах
 * @param conversions количество конверсий
 * @param profitable  флаг прибыльности кампании
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
@Schema(description = "Ответ с данными графика")
public record ChartDataResponse(

        @Schema(
                description = "Дата",
                example = "2026-06-13",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        LocalDate date,

        @Schema(
                description = "Затраты в USD",
                example = "55.65",
                minimum = "0"
        )
        double cost,

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

        @Schema(
                description = "Количество конверсий",
                example = "70",
                minimum = "0"
        )
        int conversions,

        @Schema(
                description = "Прибыльность кампании",
                example = "true"
        )
        boolean profitable
) {

    /**
     * Создает DTO из полей с автоматическим вычислением profitable.
     *
     * @param date        дата
     * @param cost        затраты
     * @param cpa         CPA
     * @param roi         ROI
     * @param conversions конверсии
     * @return готовый объект ChartDataResponse
     */
    public static ChartDataResponse of(
            LocalDate date,
            double cost,
            double cpa,
            double roi,
            int conversions
    ) {
        return new ChartDataResponse(
                date,
                cost,
                cpa,
                roi,
                conversions,
                roi > 0
        );
    }
}