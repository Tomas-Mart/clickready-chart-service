package com.clickready.chart.unit.domain;

import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import com.clickready.chart.domain.valueobject.Money;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Тесты Money Value Object")
class MoneyTest {

    @Test
    @DisplayName("Создание Money из числа")
    void shouldCreateMoneyFromDouble() {
        var money = Money.of(10.5);

        assertThat(money.getAmount()).isEqualByComparingTo("10.50");
        assertThat(money.currency()).isEqualTo(Currency.getInstance("USD"));
    }

    @Test
    @DisplayName("Создание Money из строки")
    void shouldCreateMoneyFromString() {
        var money = Money.of("15.75");

        assertThat(money.getAmount()).isEqualByComparingTo("15.75");
    }

    @Test
    @DisplayName("Выбрасывание исключения при неверном формате строки")
    void shouldThrowExceptionForInvalidString() {
        assertThatThrownBy(() -> Money.of("not-a-number"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Неверный формат суммы");
    }

    @Test
    @DisplayName("Сложение двух сумм")
    void shouldAddTwoMoneyObjects() {
        var money1 = Money.of(10.50);
        var money2 = Money.of(5.25);

        var result = money1.add(money2);

        assertThat(result.getAmount()).isEqualByComparingTo("15.75");
    }

    @Test
    @DisplayName("Выбрасывание исключения при сложении разных валют")
    void shouldThrowExceptionForDifferentCurrencies() {
        var money1 = new Money(java.math.BigDecimal.TEN, Currency.getInstance("USD"));
        var money2 = new Money(java.math.BigDecimal.ONE, Currency.getInstance("EUR"));

        assertThatThrownBy(() -> money1.add(money2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Нельзя складывать валюты");
    }

    @Test
    @DisplayName("Умножение суммы на множитель")
    void shouldMultiplyMoneyByMultiplier() {
        var money = Money.of(10.00);

        var result = money.multiply(2.5);

        assertThat(result.getAmount()).isEqualByComparingTo("25.00");
    }

    @ParameterizedTest
    @DisplayName("Проверка положительности суммы")
    @CsvSource({
            "10.0, true",
            "0.0, false",
            "-5.0, false"
    })
    void shouldCheckIfMoneyIsPositive(double amount, boolean expected) {
        var money = Money.of(amount);

        assertThat(money.isPositive()).isEqualTo(expected);
    }

    @Test
    @DisplayName("Строковое представление Money")
    void shouldFormatMoneyToString() {
        var money = Money.of(10.5);

        assertThat(money.toString()).contains("10.50");
    }
}