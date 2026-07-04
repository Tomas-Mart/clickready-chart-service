package com.clickready.chart.domain.event;

import java.time.Instant;
import java.util.UUID;
import com.clickready.chart.domain.model.ChartData;
import lombok.Getter;
import lombok.ToString;

/**
 * Доменное событие, возникающее при обновлении существующих данных графика.
 *
 * <p>Содержит информацию об обновлённых данных, включая:
 * <ul>
 *   <li>Уникальный идентификатор события</li>
 *   <li>Временную метку создания</li>
 *   <li>Обновлённые данные графика</li>
 *   <li>Дату обновляемых данных (для идентификации)</li>
 * </ul>
 *
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
@Getter
@ToString
public class ChartDataUpdatedEvent {

    /**
     * Уникальный идентификатор события.
     */
    private final String eventId;

    /**
     * Временная метка создания события.
     */
    private final Instant timestamp;

    /**
     * Обновлённые данные графика.
     */
    private final ChartData chartData;

    /**
     * Дата обновляемых данных (для идентификации).
     */
    private final String date;

    /**
     * Предыдущие данные (опционально, для отслеживания изменений).
     */
    private final ChartData previousData;

    /**
     * Конструктор события (без предыдущих данных).
     *
     * @param chartData обновлённые данные графика
     * @throws IllegalArgumentException если chartData равен null
     */
    public ChartDataUpdatedEvent(ChartData chartData) {
        this(chartData, null);
    }

    /**
     * Конструктор события (с предыдущими данными).
     *
     * @param chartData    обновлённые данные графика
     * @param previousData предыдущие данные (может быть null)
     * @throws IllegalArgumentException если chartData равен null
     */
    public ChartDataUpdatedEvent(ChartData chartData, ChartData previousData) {
        if (chartData == null) {
            throw new IllegalArgumentException("ChartData не может быть null");
        }
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.chartData = chartData;
        this.date = chartData.date() != null ? chartData.date().toString() : null;
        this.previousData = previousData;
    }

    /**
     * Проверяет, является ли событие валидным.
     *
     * @return true если событие содержит валидные данные
     */
    public boolean isValid() {
        return chartData != null && chartData.isValid();
    }

    /**
     * Проверяет, были ли данные изменены.
     *
     * @return true если предыдущие данные существуют и отличаются от текущих
     */
    public boolean hasChanges() {
        if (previousData == null) {
            return true;
        }
        return !chartData.equals(previousData);
    }

    /**
     * Проверяет, изменилось ли значение ROI.
     *
     * @return true если ROI изменился
     */
    public boolean roiChanged() {
        if (previousData == null || previousData.roi() == null) {
            return false;
        }
        return !previousData.roi().equals(chartData.roi());
    }

    /**
     * Проверяет, изменилось ли значение CPA.
     *
     * @return true если CPA изменился
     */
    public boolean cpaChanged() {
        if (previousData == null || previousData.cpa() == null) {
            return false;
        }
        return !previousData.cpa().equals(chartData.cpa());
    }

    /**
     * Проверяет, изменилось ли количество конверсий.
     *
     * @return true если конверсии изменились
     */
    public boolean conversionsChanged() {
        if (previousData == null || previousData.conversions() == null) {
            return false;
        }
        return !previousData.conversions().equals(chartData.conversions());
    }

    /**
     * Получает разницу в ROI (если изменился).
     *
     * @return разница в ROI, или 0 если нет изменений
     */
    public double getRoiDiff() {
        if (!roiChanged()) {
            return 0.0;
        }
        return chartData.roi().value() - previousData.roi().value();
    }

    /**
     * Получает разницу в конверсиях (если изменилась).
     *
     * @return разница в конверсиях, или 0 если нет изменений
     */
    public int getConversionsDiff() {
        if (!conversionsChanged()) {
            return 0;
        }
        return chartData.conversions() - previousData.conversions();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChartDataUpdatedEvent that = (ChartDataUpdatedEvent) o;
        return eventId.equals(that.eventId);
    }

    @Override
    public int hashCode() {
        return eventId.hashCode();
    }
}