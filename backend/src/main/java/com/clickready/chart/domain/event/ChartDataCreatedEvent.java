package com.clickready.chart.domain.event;

import java.time.Instant;
import java.util.UUID;
import com.clickready.chart.domain.model.ChartData;
import lombok.Getter;
import lombok.ToString;

/**
 * Доменное событие, возникающее при создании новых данных графика.
 *
 * <p>Содержит информацию о созданных данных, включая:
 * <ul>
 *   <li>Уникальный идентификатор события</li>
 *   <li>Временную метку создания</li>
 *   <li>Сами данные графика</li>
 * </ul>
 *
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
@Getter
@ToString
public class ChartDataCreatedEvent {

    /**
     * Уникальный идентификатор события.
     */
    private final String eventId;

    /**
     * Временная метка создания события.
     */
    private final Instant timestamp;

    /**
     * Созданные данные графика.
     */
    private final ChartData chartData;

    /**
     * Конструктор события.
     *
     * @param chartData созданные данные графика
     * @throws IllegalArgumentException если chartData равен null
     */
    public ChartDataCreatedEvent(ChartData chartData) {
        if (chartData == null) {
            throw new IllegalArgumentException("ChartData не может быть null");
        }
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.chartData = chartData;
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
     * Получает дату из данных графика.
     *
     * @return дата из данных
     */
    public String getDate() {
        return chartData.date() != null ? chartData.date().toString() : null;
    }

    /**
     * Получает количество конверсий из данных графика.
     *
     * @return количество конверсий
     */
    public Integer getConversions() {
        return chartData.conversions();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChartDataCreatedEvent that = (ChartDataCreatedEvent) o;
        return eventId.equals(that.eventId);
    }

    @Override
    public int hashCode() {
        return eventId.hashCode();
    }
}