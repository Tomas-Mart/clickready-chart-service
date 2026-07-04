package com.clickready.chart.application.port.in;

import java.time.LocalDate;
import java.util.List;
import com.clickready.chart.domain.model.ChartData;

/**
 * Use Case для получения данных графика.
 */
public interface GetChartDataUseCase {
    List<ChartData> getChartData();

    List<ChartData> getChartDataInRange(LocalDate startDate, LocalDate endDate);
}