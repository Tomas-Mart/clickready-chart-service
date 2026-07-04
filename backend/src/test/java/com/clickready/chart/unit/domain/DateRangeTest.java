package com.clickready.chart.unit.domain;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.clickready.chart.domain.valueobject.DateRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Тесты DateRange Value Object")
class DateRangeTest {

    @Test
    @DisplayName("Создание валидного диапазона")
    void shouldCreateValidDateRange() {
        var start = LocalDate.of(2026, 6, 1);
        var end = LocalDate.of(2026, 6, 30);

        var range = new DateRange(start, end);

        assertThat(range.startDate()).isEqualTo(start);
        assertThat(range.endDate()).isEqualTo(end);
    }

    @Test
    @DisplayName("Выбрасывание исключения при null датах")
    void shouldThrowExceptionForNullDates() {
        assertThatThrownBy(() -> new DateRange(null, LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("не могут быть null");
    }

    @Test
    @DisplayName("Выбрасывание исключения когда start > end")
    void shouldThrowExceptionWhenStartAfterEnd() {
        var start = LocalDate.of(2026, 6, 30);
        var end = LocalDate.of(2026, 6, 1);

        assertThatThrownBy(() -> new DateRange(start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Начальная дата не может быть позже конечной");
    }

    @Test
    @DisplayName("Вычисление количества дней в диапазоне")
    void shouldCalculateDaysBetween() {
        var range = new DateRange(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30)
        );

        assertThat(range.daysBetween()).isEqualTo(29);
    }

    @Test
    @DisplayName("Проверка вхождения даты в диапазон")
    void shouldCheckIfDateInRange() {
        var range = new DateRange(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30)
        );

        assertThat(range.contains(LocalDate.of(2026, 6, 15))).isTrue();
        assertThat(range.contains(LocalDate.of(2026, 5, 31))).isFalse();
        assertThat(range.contains(LocalDate.of(2026, 7, 1))).isFalse();
    }
}