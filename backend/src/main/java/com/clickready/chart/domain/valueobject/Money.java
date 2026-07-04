package com.clickready.chart.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Value Object для работы с деньгами.
 *
 * <p>Обеспечивает безопасные операции с валютой.
 * Использует record для неизменяемости.
 *
 * @param amount   сумма в долларах
 * @param currency валюта
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
public record Money(
        BigDecimal amount,
        Currency currency
) {

    /**
     * Нулевая сумма в USD.
     */
    public static final Money ZERO = Money.of(0);

    /**
     * Конструктор с нормализацией.
     */
    public Money {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        if (currency == null) {
            currency = Currency.getInstance("USD");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Создает Money из числа (USD).
     *
     * @param amount сумма в долларах
     * @return объект Money
     */
    public static Money of(double amount) {
        return new Money(
                BigDecimal.valueOf(amount),
                Currency.getInstance("USD")
        );
    }

    /**
     * Создает Money из строки.
     *
     * @param amount сумма в долларах
     * @return объект Money
     * @throws IllegalArgumentException если сумма невалидна
     */
    public static Money of(String amount) {
        try {
            return of(Double.parseDouble(amount));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Неверный формат суммы: " + amount, e);
        }
    }

    /**
     * Возвращает сумму как double.
     *
     * @return сумма в double
     */
    public double getValue() {
        return amount.doubleValue();
    }

    /**
     * Возвращает сумму как BigDecimal.
     *
     * @return сумма в BigDecimal
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Проверяет, является ли сумма положительной.
     *
     * @return true если > 0
     */
    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Проверяет, является ли сумма нулевой.
     *
     * @return true если равна 0
     */
    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Проверяет, является ли сумма отрицательной.
     *
     * @return true если < 0
     */
    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Складывает два значения Money.
     *
     * @param other другое значение Money
     * @return новый объект Money
     * @throws IllegalArgumentException если валюты не совпадают
     */
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Нельзя складывать валюты: " + this.currency + " и " + other.currency
            );
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * Вычитает одно значение Money из другого.
     *
     * @param other другое значение Money
     * @return новый объект Money
     */
    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Нельзя вычитать валюты: " + this.currency + " и " + other.currency
            );
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    /**
     * Умножает сумму на множитель.
     *
     * @param multiplier множитель
     * @return новый объект Money
     */
    public Money multiply(double multiplier) {
        return new Money(
                this.amount.multiply(BigDecimal.valueOf(multiplier)),
                this.currency
        );
    }

    /**
     * Делит сумму на делитель.
     *
     * @param divisor делитель
     * @return новый объект Money
     * @throws ArithmeticException если делитель равен 0
     */
    public Money divide(int divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("Деление на ноль");
        }
        return new Money(
                this.amount.divide(BigDecimal.valueOf(divisor), RoundingMode.HALF_UP),
                this.currency
        );
    }

    /**
     * Сравнивает два значения Money.
     *
     * @param other другое значение Money
     * @return -1, 0, 1 в зависимости от сравнения
     */
    public int compareTo(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Нельзя сравнивать валюты: " + this.currency + " и " + other.currency
            );
        }
        return this.amount.compareTo(other.amount);
    }

    @Override
    public String toString() {
        return String.format("%.2f %s", amount, currency.getSymbol());
    }
}