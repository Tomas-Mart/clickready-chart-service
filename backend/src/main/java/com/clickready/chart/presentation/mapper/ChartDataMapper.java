package com.clickready.chart.presentation.mapper;

import java.util.List;
import com.clickready.chart.domain.model.ChartData;
import com.clickready.chart.infrastructure.repository.entity.ChartDataEntity;
import com.clickready.chart.presentation.dto.ChartDataRequest;
import com.clickready.chart.presentation.dto.ChartDataResponse;

/**
 * Контракт маппера для преобразования между слоями приложения.
 *
 * <p>Отвечает за конвертацию между:
 * <ul>
 *   <li>Domain Model (ChartData) — бизнес-логика</li>
 *   <li>Entity (ChartDataEntity) — JPA сущность</li>
 *   <li>DTO (ChartDataRequest/Response) — API контракты</li>
 * </ul>
 *
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
public interface ChartDataMapper {

    // ============================================================
    // DTO -> DOMAIN
    // ============================================================

    /**
     * Преобразует DTO запроса в Domain модель.
     *
     * @param request DTO запроса
     * @return Domain модель ChartData
     */
    ChartData toDomain(ChartDataRequest request);

    /**
     * Преобразует список DTO запросов в список Domain моделей.
     *
     * @param requests Список DTO запросов
     * @return Список Domain моделей
     */
    List<ChartData> toDomainListFromRequests(List<ChartDataRequest> requests);

    // ============================================================
    // ENTITY -> DOMAIN
    // ============================================================

    /**
     * Преобразует JPA сущность в Domain модель.
     *
     * @param entity JPA сущность
     * @return Domain модель ChartData
     */
    ChartData toDomain(ChartDataEntity entity);

    /**
     * Преобразует список JPA сущностей в список Domain моделей.
     *
     * @param entities Список JPA сущностей
     * @return Список Domain моделей
     */
    List<ChartData> toDomainListFromEntities(List<ChartDataEntity> entities);

    // ============================================================
    // DOMAIN -> DTO
    // ============================================================

    /**
     * Преобразует Domain модель в DTO ответа.
     *
     * @param domain Domain модель
     * @return DTO ответа
     */
    ChartDataResponse toResponse(ChartData domain);

    /**
     * Безопасно преобразует список объектов в DTO ответов.
     * Поддерживает ChartData и LinkedHashMap (из кэша Redis).
     *
     * @param objects список объектов (ChartData или LinkedHashMap)
     * @return Список DTO ответов
     */
    List<ChartDataResponse> toResponseList(List<?> objects);

    // ============================================================
    // DOMAIN -> ENTITY
    // ============================================================

    /**
     * Преобразует Domain модель в JPA сущность.
     *
     * @param domain Domain модель
     * @return JPA сущность
     */
    ChartDataEntity toEntity(ChartData domain);

    /**
     * Преобразует список Domain моделей в список JPA сущностей.
     *
     * @param domains Список Domain моделей
     * @return Список JPA сущностей
     */
    List<ChartDataEntity> toEntityList(List<ChartData> domains);
}