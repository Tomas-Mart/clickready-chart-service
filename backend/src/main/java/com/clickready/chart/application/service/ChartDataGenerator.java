package com.clickready.chart.application.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;
import com.clickready.chart.domain.model.ChartData;
import com.clickready.chart.domain.valueobject.Cpa;
import com.clickready.chart.domain.valueobject.Money;
import com.clickready.chart.domain.valueobject.Roi;
import lombok.extern.slf4j.Slf4j;

/**
 * Генератор тестовых данных для графика.
 */
@Slf4j
@Component
public class ChartDataGenerator {

    /**
     * Генерирует набор тестовых данных по умолчанию.
     */
    public List<ChartData> generateDefaultData() {
        log.info("Генерация тестовых данных для графика");

        return List.of(
                createDataPoint(LocalDate.of(2026, 6, 10), 0, 0, 100, 0),
                createDataPoint(LocalDate.of(2026, 6, 11), 20, 0.5, 45, 10),
                createDataPoint(LocalDate.of(2026, 6, 12), 44.36, 1.23, 161.47, 36),
                createDataPoint(LocalDate.of(2026, 6, 13), 55.65, 0.79, 56.33, 70),
                createDataPoint(LocalDate.of(2026, 6, 14), 80, 0.2, 220, 110),
                createDataPoint(LocalDate.of(2026, 6, 15), 65, 0.45, 180, 95),
                createDataPoint(LocalDate.of(2026, 6, 16), 90, 0.35, 250, 130)
        );
    }

    /**
     * Создает одну точку данных.
     */
    private ChartData createDataPoint(LocalDate date, double cost, double cpa, double roi, int conversions) {
        return ChartData.builder()
                .date(date)
                .cost(Money.of(cost))
                .cpa(Cpa.of(cpa))
                .roi(Roi.of(roi))
                .conversions(conversions)
                .build();
    }
}