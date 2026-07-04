package com.clickready.chart.unit.domain;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.clickready.chart.domain.model.ChartData;
import com.clickready.chart.domain.valueobject.Cpa;
import com.clickready.chart.domain.valueobject.Money;
import com.clickready.chart.domain.valueobject.Roi;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тесты доменной модели ChartData")
class ChartDataTest {

    private static final LocalDate TEST_DATE = LocalDate.of(2026, 6, 13);
    private static final double TEST_COST = 55.65;
    private static final double TEST_CPA = 0.79;
    private static final double TEST_ROI = 56.33;
    private static final int TEST_CONVERSIONS = 70;

    @Test
    @DisplayName("Создание валидной модели ChartData")
    void shouldCreateValidChartData() {
        var data = new ChartData(
                TEST_DATE,
                Money.of(TEST_COST),
                Cpa.of(TEST_CPA),
                Roi.of(TEST_ROI),
                TEST_CONVERSIONS
        );

        assertThat(data.isValid()).isTrue();
        assertThat(data.isProfitable()).isTrue();
        assertThat(data.date()).isEqualTo(TEST_DATE);
        assertThat(data.cost().getValue()).isEqualTo(TEST_COST);
        assertThat(data.conversions()).isEqualTo(TEST_CONVERSIONS);
    }

    @Test
    @DisplayName("Проверка невалидных данных - null дата")
    void shouldDetectInvalidDataWithNullDate() {
        // ✅ Проверяем isValid() возвращает false, а не исключение
        var data = new ChartData(
                null,
                Money.of(TEST_COST),
                Cpa.of(TEST_CPA),
                Roi.of(TEST_ROI),
                TEST_CONVERSIONS
        );

        assertThat(data.isValid()).isFalse();
        assertThat(data.date()).isNull();
    }

    @Test
    @DisplayName("Проверка невалидных данных - отрицательные конверсии")
    void shouldDetectInvalidDataWithNegativeConversions() {
        // ✅ Проверяем isValid() возвращает false, а не исключение
        var data = new ChartData(
                TEST_DATE,
                Money.of(TEST_COST),
                Cpa.of(TEST_CPA),
                Roi.of(TEST_ROI),
                -5
        );

        assertThat(data.isValid()).isFalse();
        assertThat(data.conversions()).isEqualTo(-5);
    }

    @Test
    @DisplayName("Расчет дохода на основе конверсий и CPA")
    void shouldCalculateRevenue() {
        var data = new ChartData(
                TEST_DATE,
                Money.of(TEST_COST),
                Cpa.of(TEST_CPA),
                Roi.of(TEST_ROI),
                TEST_CONVERSIONS
        );

        var revenue = data.calculateRevenue();
        assertThat(revenue.getValue()).isEqualTo(55.30);
    }

    @Test
    @DisplayName("Расчет дохода при null CPA - возвращает ZERO")
    void shouldReturnZeroRevenueWhenCpaIsNull() {
        // ✅ Вместо исключения проверяем, что revenue = ZERO
        var data = new ChartData(
                TEST_DATE,
                Money.of(TEST_COST),
                null,
                Roi.of(TEST_ROI),
                TEST_CONVERSIONS
        );

        assertThat(data.isValid()).isFalse();
        assertThat(data.cpa()).isNull();
        assertThat(data.calculateRevenue()).isEqualTo(Money.ZERO);
    }

    @Test
    @DisplayName("Расчет прибыли")
    void shouldCalculateProfit() {
        var data = new ChartData(
                TEST_DATE,
                Money.of(TEST_COST),
                Cpa.of(TEST_CPA),
                Roi.of(TEST_ROI),
                TEST_CONVERSIONS
        );

        var profit = data.calculateProfit();
        assertThat(profit.getValue()).isEqualTo(-0.35);
    }

    @Test
    @DisplayName("Проверка убыточности")
    void shouldDetectLoss() {
        var data = new ChartData(
                TEST_DATE,
                Money.of(100),
                Cpa.of(1.5),
                Roi.of(-20.0),
                50
        );

        assertThat(data.isProfitable()).isFalse();
        assertThat(data.isLoss()).isTrue();
    }

    @Test
    @DisplayName("Строковое представление ChartData")
    void shouldFormatChartDataToString() {
        var data = new ChartData(
                TEST_DATE,
                Money.of(TEST_COST),
                Cpa.of(TEST_CPA),
                Roi.of(TEST_ROI),
                TEST_CONVERSIONS
        );

        assertThat(data.toString())
                .contains("date=2026-06-13")
                .contains("cost=55.65")
                .contains("cpa=0.79")
                .contains("roi=56.33")
                .contains("conversions=70");
    }
}