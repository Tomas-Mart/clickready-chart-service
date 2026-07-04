package com.clickready.chart.domain.valueobject;

import java.math.BigDecimal;

/**
 * Value Object для ROI (Return on Investment).
 *
 * <p>Хранит значение ROI в процентах и предоставляет
 * методы для проверки прибыльности.
 *
 * @param value значение ROI в процентах
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
public record Roi(double value) {

    /**
     * Конструктор с валидацией.
     *
     * @param value значение ROI
     * @throws IllegalArgumentException если значение некорректно
     */
    public Roi {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("ROI не может быть NaN или бесконечностью: " + value);
        }
    }

    /**
     * Создает ROI из числа.
     *
     * @param value значение ROI
     * @return объект Roi
     */
    public static Roi of(double value) {
        return new Roi(value);
    }

    /**
     * Создает ROI из строки.
     *
     * @param value значение ROI в виде строки
     * @return объект Roi
     * @throws IllegalArgumentException если строка не может быть преобразована в число
     */
    public static Roi of(String value) {
        try {
            return new Roi(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Неверный формат ROI: " + value, e);
        }
    }

    /**
     * Возвращает значение как double.
     *
     * @return значение ROI
     */
    public double getValue() {
        return value;
    }

    /**
     * Преобразует значение в BigDecimal.
     * Используется для сохранения в БД.
     *
     * @return значение ROI как BigDecimal
     */
    public BigDecimal getBigDecimal() {
        return BigDecimal.valueOf(value);
    }

    /**
     * Проверяет, является ли ROI прибыльным.
     *
     * @return true если ROI > 0
     */
    public boolean isProfitable() {
        return value > 0;
    }

    /**
     * Проверяет, является ли ROI очень прибыльным (> 100%).
     *
     * @return true если ROI > 100
     */
    public boolean isHighlyProfitable() {
        return value > 100;
    }

    /**
     * Проверяет, является ли ROI убыточным.
     *
     * @return true если ROI < 0
     */
    public boolean isLoss() {
        return value < 0;
    }

    /**
     * Складывает два ROI.
     *
     * @param other другой ROI
     * @return новый ROI
     */
    public Roi add(Roi other) {
        return new Roi(this.value + other.value);
    }

    @Override
    public String toString() {
        return String.format("%.2f%%", value);
    }
}