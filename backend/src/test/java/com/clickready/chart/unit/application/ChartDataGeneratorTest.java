package com.clickready.chart.unit.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.clickready.chart.application.service.ChartDataGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тесты генератора данных")
class ChartDataGeneratorTest {

    private final ChartDataGenerator generator = new ChartDataGenerator();

    @Test
    @DisplayName("Генерация тестовых данных")
    void shouldGenerateDefaultData() {
        var data = generator.generateDefaultData();

        assertThat(data).isNotEmpty();
        assertThat(data).hasSize(7);

        // Проверяем первую точку
        var first = data.getFirst();
        assertThat(first.date()).isEqualTo(java.time.LocalDate.of(2026, 6, 10));
        assertThat(first.conversions()).isZero();

        // Проверяем последнюю точку
        var last = data.getLast();
        assertThat(last.date()).isEqualTo(java.time.LocalDate.of(2026, 6, 16));
        assertThat(last.conversions()).isEqualTo(130);
    }

    @Test
    @DisplayName("Все сгенерированные данные валидны")
    void shouldGenerateValidData() {
        var data = generator.generateDefaultData();

        data.forEach(point -> {
            assertThat(point.isValid()).isTrue();
            assertThat(point.conversions()).isGreaterThanOrEqualTo(0);
            assertThat(point.cost().isPositive() || point.cost().getAmount().doubleValue() == 0)
                    .isTrue();
        });
    }
}