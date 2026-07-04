package com.clickready.chart.application.port.in;

import java.time.LocalDate;

/**
 * Use Case для обновления данных графика.
 */
public interface UpdateChartDataUseCase {
    void deleteChartData(LocalDate date);
}