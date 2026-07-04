package com.clickready.chart.unit.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import com.clickready.chart.domain.valueobject.Cpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Тесты CPA Value Object")
class CpaTest {

    @Test
    @DisplayName("Создание CPA из числа")
    void shouldCreateCpaFromDouble() {
        var cpa = Cpa.of(0.79);

        assertThat(cpa.getValue()).isEqualTo(0.79);
    }

    @Test
    @DisplayName("Выбрасывание исключения при отрицательном CPA")
    void shouldThrowExceptionForNegativeCpa() {
        assertThatThrownBy(() -> Cpa.of(-1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CPA не может быть отрицательным");
    }

    @Test
    @DisplayName("Создание CPA из строки")
    void shouldCreateCpaFromString() {
        var cpa = Cpa.of("1.23");

        assertThat(cpa.getValue()).isEqualTo(1.23);
    }

    @ParameterizedTest
    @DisplayName("Проверка эффективности CPA (< 1)")
    @CsvSource({
            "0.79, true",
            "1.23, false",
            "0.5, true"
    })
    void shouldCheckIfCpaIsEfficient(double value, boolean expected) {
        var cpa = Cpa.of(value);

        assertThat(cpa.isEfficient()).isEqualTo(expected);
    }

    @Test
    @DisplayName("Строковое представление CPA")
    void shouldFormatCpaToString() {
        var cpa = Cpa.of(0.79);

        assertThat(cpa.toString()).isEqualTo("0.79");
    }
}