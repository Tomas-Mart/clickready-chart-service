package com.clickready.chart.application.port.in;

import java.util.List;
import com.clickready.chart.domain.model.ChartData;

/**
 * Use Case для сохранения данных графика.
 */
public interface SaveChartDataUseCase {
    ChartData saveChartData(ChartData data);

    List<ChartData> saveAllChartData(List<ChartData> dataList);
}