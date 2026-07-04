package com.clickready.chart.unit.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import com.clickready.chart.domain.valueobject.Roi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Тесты ROI Value Object")
class RoiTest {

    @Test
    @DisplayName("Создание ROI из числа")
    void shouldCreateRoiFromDouble() {
        var roi = Roi.of(56.33);

        assertThat(roi.value()).isEqualTo(56.33);
    }

    @Test
    @DisplayName("Создание ROI из строки")
    void shouldCreateRoiFromString() {
        var roi = Roi.of("161.47");

        assertThat(roi.value()).isEqualTo(161.47);
    }

    @Test
    @DisplayName("Выбрасывание исключения при неверном формате строки")
    void shouldThrowExceptionForInvalidString() {
        assertThatThrownBy(() -> Roi.of("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Неверный формат ROI");
    }

    @ParameterizedTest
    @DisplayName("Проверка прибыльности ROI")
    @CsvSource({
            "56.33, true",
            "0.0, false",
            "-10.0, false"
    })
    void shouldCheckIfRoiIsProfitable(double value, boolean expected) {
        var roi = Roi.of(value);

        assertThat(roi.isProfitable()).isEqualTo(expected);
    }

    @ParameterizedTest
    @DisplayName("Проверка высокой прибыльности ROI (>100%)")
    @CsvSource({
            "161.47, true",
            "56.33, false",
            "100.0, false"
    })
    void shouldCheckIfRoiIsHighlyProfitable(double value, boolean expected) {
        var roi = Roi.of(value);

        assertThat(roi.isHighlyProfitable()).isEqualTo(expected);
    }

    @Test
    @DisplayName("Строковое представление ROI")
    void shouldFormatRoiToString() {
        var roi = Roi.of(56.33);

        assertThat(roi.toString()).isEqualTo("56.33%");
    }
}