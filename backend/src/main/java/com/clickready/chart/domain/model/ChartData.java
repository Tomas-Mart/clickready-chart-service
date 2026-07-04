package com.clickready.chart.domain.model;

import java.time.LocalDate;
import com.clickready.chart.domain.valueobject.Cpa;
import com.clickready.chart.domain.valueobject.Money;
import com.clickready.chart.domain.valueobject.Roi;
import lombok.Builder;

/**
 * Доменная модель данных графика.
 *
 * <p>Использует Record для иммутабельности и автоматической генерации методов.
 * Вся бизнес-валидация выполняется в сервисном слое.
 *
 * @param date        дата, за которую сохранены данные
 * @param cost        затраты в USD (Value Object Money)
 * @param cpa         стоимость привлечения клиента (Value Object Cpa)
 * @param roi         возврат инвестиций в процентах (Value Object Roi)
 * @param conversions количество конверсий
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
@Builder(toBuilder = true)
public record ChartData(
        LocalDate date,
        Money cost,
        Cpa cpa,
        Roi roi,
        Integer conversions
) {

    /**
     * Проверяет, является ли объект действительным.
     * Все поля должны быть заполнены и валидны.
     *
     * @return true если все поля валидны
     */
    public boolean isValid() {
        return date != null
               && cost != null
               && cpa != null
               && roi != null
               && conversions != null
               && conversions >= 0;
    }

    /**
     * Проверяет, прибыльна ли кампания.
     *
     * @return true если ROI > 0
     */
    public boolean isProfitable() {
        return roi != null && roi.isProfitable();
    }

    /**
     * Проверяет, является ли кампания очень прибыльной (> 100%).
     *
     * @return true если ROI > 100
     */
    public boolean isHighlyProfitable() {
        return roi != null && roi.isHighlyProfitable();
    }

    /**
     * Проверяет, является ли CPA эффективным (< 1).
     *
     * @return true если CPA < 1
     */
    public boolean isCpaEfficient() {
        return cpa != null && cpa.isEfficient();
    }

    /**
     * Вычисляет общий доход на основе конверсий и CPA.
     *
     * <p>Формула: revenue = cpa * conversions
     *
     * @return расчетный доход как Money
     */
    public Money calculateRevenue() {
        if (cpa == null || conversions == null || conversions == 0) {
            return Money.ZERO;
        }
        double revenueValue = cpa.getValue() * conversions;
        return Money.of(revenueValue);
    }

    /**
     * Вычисляет общую прибыль.
     *
     * <p>Формула: profit = revenue - cost
     *
     * @return прибыль как Money
     */
    public Money calculateProfit() {
        Money revenue = calculateRevenue();
        return revenue.subtract(cost);
    }

    /**
     * Проверяет, является ли кампания убыточной.
     *
     * @return true если ROI < 0
     */
    public boolean isLoss() {
        return roi != null && roi.isLoss();
    }

    /**
     * Вычисляет маржинальность в процентах.
     *
     * <p>Формула: margin = (revenue - cost) / revenue * 100
     *
     * @return маржинальность в процентах
     */
    public double calculateMargin() {
        Money revenue = calculateRevenue();
        if (revenue.isZero()) {
            return 0.0;
        }
        Money profit = revenue.subtract(cost);
        return (profit.getValue() / revenue.getValue()) * 100;
    }

    /**
     * Возвращает количество конверсий как int.
     * Для безопасной работы с null.
     *
     * @return количество конверсий или 0 если null
     */
    public int getConversionsSafe() {
        return conversions != null ? conversions : 0;
    }

    /**
     * Возвращает затраты как double.
     * Для безопасной работы с null.
     *
     * @return затраты или 0.0 если null
     */
    public double getCostSafe() {
        return cost != null ? cost.getValue() : 0.0;
    }

    /**
     * Создает копию объекта с обновленной датой.
     *
     * @param newDate новая дата
     * @return новый объект ChartData
     */
    public ChartData withDate(LocalDate newDate) {
        return new ChartData(newDate, cost, cpa, roi, conversions);
    }

    /**
     * Создает копию объекта с обновленными затратами.
     *
     * @param newCost новые затраты
     * @return новый объект ChartData
     */
    public ChartData withCost(Money newCost) {
        return new ChartData(date, newCost, cpa, roi, conversions);
    }

    /**
     * Создает копию объекта с обновленным CPA.
     *
     * @param newCpa новое CPA
     * @return новый объект ChartData
     */
    public ChartData withCpa(Cpa newCpa) {
        return new ChartData(date, cost, newCpa, roi, conversions);
    }

    /**
     * Создает копию объекта с обновленным ROI.
     *
     * @param newRoi новый ROI
     * @return новый объект ChartData
     */
    public ChartData withRoi(Roi newRoi) {
        return new ChartData(date, cost, cpa, newRoi, conversions);
    }

    /**
     * Создает копию объекта с обновленными конверсиями.
     *
     * @param newConversions новые конверсии
     * @return новый объект ChartData
     */
    public ChartData withConversions(Integer newConversions) {
        return new ChartData(date, cost, cpa, roi, newConversions);
    }

    @Override
    public String toString() {
        return String.format(
                "ChartData[date=%s, cost=%.2f, cpa=%.2f, roi=%.2f%%, conversions=%d]",
                date,
                cost != null ? cost.getValue() : 0.0,
                cpa != null ? cpa.getValue() : 0.0,
                roi != null ? roi.getValue() : 0.0,
                conversions != null ? conversions : 0
        );
    }
}