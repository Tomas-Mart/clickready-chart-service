package com.clickready.chart.infrastructure.repository.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA сущность для хранения данных графика в базе данных.
 *
 * <p>Представляет собой запись с данными за конкретную дату.
 * Использует оптимистичные блокировки через {@code @Version}
 * для предотвращения конкурентных обновлений.
 *
 * <p><b>Индексы:</b>
 * <ul>
 *   <li>{@code idx_chart_data_date} — для быстрого поиска по дате</li>
 *   <li>{@code idx_chart_data_date_cost} — для аналитических запросов</li>
 * </ul>
 *
 * <p><b>Уникальность:</b>
 * <ul>
 *   <li>{@code uk_chart_data_date} — гарантирует уникальность дат</li>
 * </ul>
 *
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@Table(
        name = "chart_data",
        indexes = {
                @Index(name = "idx_chart_data_date", columnList = "date"),
                @Index(name = "idx_chart_data_date_cost", columnList = "date, cost")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chart_data_date", columnNames = "date")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class ChartDataEntity {

    /**
     * Уникальный идентификатор записи.
     * Генерируется автоматически при сохранении.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Дата, за которую сохранены данные.
     * Не может быть null и должна быть уникальной.
     */
    @Column(name = "date", nullable = false)
    private LocalDate date;

    /**
     * Затраты в USD.
     * Хранится с точностью до 2 знаков после запятой.
     */
    @Column(name = "cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal cost;

    /**
     * Cost Per Acquisition — стоимость привлечения одного клиента.
     * Хранится с точностью до 2 знаков после запятой.
     */
    @Column(name = "cpa", nullable = false, precision = 10, scale = 2)
    private BigDecimal cpa;

    /**
     * Return on Investment — возврат инвестиций в процентах.
     * Хранится с точностью до 2 знаков после запятой.
     */
    @Column(name = "roi", nullable = false, precision = 10, scale = 2)
    private BigDecimal roi;

    /**
     * Количество конверсий.
     * Не может быть null.
     */
    @Column(name = "conversions", nullable = false)
    private Integer conversions;

    /**
     * Версия для оптимистичной блокировки.
     * Автоматически увеличивается при каждом обновлении.
     * Начальное значение: 0.
     */
    @Version
    @Builder.Default
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    /**
     * Дата и время создания записи.
     * Устанавливается автоматически при первом сохранении.
     * Не может быть изменено после создания.
     */
    @CreatedDate
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления записи.
     * Обновляется автоматически при каждом изменении.
     */
    @LastModifiedDate
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Проверяет, является ли сущность новой (ещё не сохранена в БД).
     *
     * @return true если сущность новая (id == null)
     */
    @Transient
    public boolean isNew() {
        return id == null;
    }

    /**
     * Проверяет, является ли кампания прибыльной.
     *
     * @return true если ROI > 0
     */
    @Transient
    public boolean isProfitable() {
        return roi != null && roi.compareTo(BigDecimal.ZERO) > 0;
    }
}