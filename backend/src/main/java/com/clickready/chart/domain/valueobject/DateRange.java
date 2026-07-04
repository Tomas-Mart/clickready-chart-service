package com.clickready.chart.domain.valueobject;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Value Object для диапазона дат.
 *
 * <p>Обеспечивает валидацию дат и полезные методы
 * для работы с диапазонами.
 *
 * @param startDate начальная дата
 * @param endDate   конечная дата
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
public record DateRange(
        LocalDate startDate,
        LocalDate endDate
) {

    /**
     * Конструктор с валидацией.
     *
     * @param startDate начальная дата
     * @param endDate   конечная дата
     * @throws IllegalArgumentException если даты некорректны
     */
    public DateRange {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Даты не могут быть null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Начальная дата не может быть позже конечной: " + startDate + " > " + endDate
            );
        }
    }

    /**
     * Создает DateRange из двух дат.
     *
     * @param startDate начальная дата
     * @param endDate   конечная дата
     * @return объект DateRange
     */
    public static DateRange of(LocalDate startDate, LocalDate endDate) {
        return new DateRange(startDate, endDate);
    }

    /**
     * Вычисляет количество дней в диапазоне.
     *
     * @return количество дней
     */
    public long daysBetween() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Проверяет, содержит ли диапазон указанную дату.
     *
     * @param date дата для проверки
     * @return true если дата входит в диапазон
     */
    public boolean contains(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Проверяет, пересекается ли диапазон с другим.
     *
     * @param other другой диапазон
     * @return true если пересекаются
     */
    public boolean overlaps(DateRange other) {
        return !this.endDate.isBefore(other.startDate) &&
               !this.startDate.isAfter(other.endDate);
    }

    @Override
    public String toString() {
        return startDate + " -> " + endDate + " (" + daysBetween() + " days)";
    }
}