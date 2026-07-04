package com.clickready.chart.domain.valueobject;

import java.math.BigDecimal;

/**
 * Value Object для CPA (Cost Per Acquisition).
 *
 * <p>Стоимость привлечения одного клиента.
 * Использует record для неизменяемости.
 *
 * @param value значение CPA
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
public record Cpa(double value) {

    /**
     * Конструктор с валидацией.
     *
     * @param value значение CPA
     * @throws IllegalArgumentException если CPA отрицательный или некорректен
     */
    public Cpa {
        if (value < 0) {
            throw new IllegalArgumentException("CPA не может быть отрицательным: " + value);
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("CPA не может быть NaN или бесконечностью: " + value);
        }
    }

    /**
     * Создает CPA из числа.
     *
     * @param value значение CPA
     * @return объект Cpa
     */
    public static Cpa of(double value) {
        return new Cpa(value);
    }

    /**
     * Создает CPA из строки.
     *
     * @param value значение CPA в виде строки
     * @return объект Cpa
     * @throws IllegalArgumentException если строка не может быть преобразована
     */
    public static Cpa of(String value) {
        try {
            return new Cpa(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Неверный формат CPA: " + value, e);
        }
    }

    /**
     * Возвращает значение как double.
     *
     * @return значение CPA
     */
    public double getValue() {
        return value;
    }

    /**
     * Преобразует значение в BigDecimal.
     * Используется для сохранения в БД.
     *
     * @return значение CPA как BigDecimal
     */
    public BigDecimal getBigDecimal() {
        return BigDecimal.valueOf(value);
    }

    /**
     * Проверяет, является ли CPA эффективным (менее 1).
     *
     * @return true если CPA < 1
     */
    public boolean isEfficient() {
        return value < 1.0;
    }

    /**
     * Проверяет, является ли CPA очень эффективным (менее 0.5).
     *
     * @return true если CPA < 0.5
     */
    public boolean isVeryEfficient() {
        return value < 0.5;
    }

    /**
     * Проверяет, является ли CPA дорогим (более 2).
     *
     * @return true если CPA > 2
     */
    public boolean isExpensive() {
        return value > 2.0;
    }

    @Override
    public String toString() {
        return String.format("%.2f", value);
    }
}