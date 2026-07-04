package com.clickready.chart.presentation.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.clickready.chart.domain.model.ChartData;
import com.clickready.chart.domain.valueobject.Cpa;
import com.clickready.chart.domain.valueobject.Money;
import com.clickready.chart.domain.valueobject.Roi;
import com.clickready.chart.infrastructure.repository.entity.ChartDataEntity;
import com.clickready.chart.presentation.dto.ChartDataRequest;
import com.clickready.chart.presentation.dto.ChartDataResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Реализация маппера для преобразования между слоями приложения.
 *
 * <p>Использует ручную конвертацию для максимальной гибкости
 * и прозрачности преобразований.
 *
 * <p><b>Преимущества ручной реализации:</b>
 * <ul>
 *   <li>Полный контроль над преобразованиями</li>
 *   <li>Легкая отладка</li>
 *   <li>Читаемый код</li>
 *   <li>Простая документация</li>
 *   <li>Быстрая компиляция</li>
 * </ul>
 *
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
@Slf4j
@Component
public class ChartDataMapperImpl implements ChartDataMapper {

    private static final String ERROR_REQUEST_NULL = "ChartDataRequest не может быть null";
    private static final String ERROR_ENTITY_NULL = "ChartDataEntity не может быть null";
    private static final String ERROR_DOMAIN_NULL = "ChartData не может быть null";

    // ============================================================
    // DTO -> DOMAIN
    // ============================================================

    /**
     * {@inheritDoc}
     *
     * <p>Создает Value Objects из примитивов с валидацией.
     */
    @Override
    public ChartData toDomain(ChartDataRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(ERROR_REQUEST_NULL);
        }

        log.debug("Преобразование Request -> Domain: date={}", request.date());

        try {
            return new ChartData(
                    request.date(),
                    Money.of(request.cost()),
                    Cpa.of(request.cpa()),
                    Roi.of(request.roi()),
                    request.conversions()
            );
        } catch (Exception e) {
            log.error("Ошибка преобразования Request -> Domain: {}", e.getMessage(), e);
            throw new IllegalStateException("Ошибка маппинга данных", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChartData> toDomainListFromRequests(List<ChartDataRequest> requests) {
        if (requests == null) {
            return List.of();
        }
        return requests.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    // ============================================================
    // ENTITY -> DOMAIN
    // ============================================================

    /**
     * {@inheritDoc}
     *
     * <p>Преобразует JPA сущность в Domain модель.
     * Конвертирует BigDecimal в double через Value Objects.
     */
    @Override
    public ChartData toDomain(ChartDataEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException(ERROR_ENTITY_NULL);
        }

        log.debug("Преобразование Entity -> Domain: id={}, date={}",
                entity.getId(), entity.getDate());

        return new ChartData(
                entity.getDate(),
                Money.of(entity.getCost().doubleValue()),
                Cpa.of(entity.getCpa().doubleValue()),
                Roi.of(entity.getRoi().doubleValue()),
                entity.getConversions()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChartData> toDomainListFromEntities(List<ChartDataEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    // ============================================================
    // DOMAIN -> DTO
    // ============================================================

    /**
     * {@inheritDoc}
     *
     * <p>Преобразует Domain модель в DTO ответа.
     * Извлекает примитивы из Value Objects.
     */
    @Override
    public ChartDataResponse toResponse(ChartData domain) {
        if (domain == null) {
            throw new IllegalArgumentException(ERROR_DOMAIN_NULL);
        }

        return new ChartDataResponse(
                domain.date(),
                domain.cost().getValue(),
                domain.cpa().getValue(),
                domain.roi().getValue(),
                domain.conversions(),
                domain.isProfitable()
        );
    }

    /**
     * {@inheritDoc}
     *
     * <p>Безопасно преобразует список объектов в DTO ответов.
     * Поддерживает ChartData и LinkedHashMap (из кэша Redis).
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<ChartDataResponse> toResponseList(List<?> objects) {
        if (objects == null) {
            return List.of();
        }

        return objects.stream()
                .map(item -> {
                    // Если это ChartData — преобразуем напрямую
                    if (item instanceof ChartData chartData) {
                        return toResponse(chartData);
                    }

                    // Если это Map (из кэша Redis) — восстанавливаем ChartData
                    if (item instanceof Map<?, ?> map) {
                        log.debug("Восстановление ChartData из Map (кэш): {}", map);
                        ChartData recovered = recoverFromMap(map);
                        return toResponse(recovered);
                    }

                    // Неизвестный тип — ошибка
                    log.error("Неизвестный тип в списке: {}",
                            item != null ? item.getClass().getName() : "null");
                    throw new IllegalArgumentException(
                            "Элемент не является ChartData или Map: " +
                            (item != null ? item.getClass().getName() : "null")
                    );
                })
                .collect(Collectors.toList());
    }

    // ============================================================
    // DOMAIN -> ENTITY
    // ============================================================

    /**
     * {@inheritDoc}
     *
     * <p>Преобразует Domain модель в JPA сущность.
     * Конвертирует Value Objects в BigDecimal для БД.
     */
    @Override
    public ChartDataEntity toEntity(ChartData domain) {
        if (domain == null) {
            throw new IllegalArgumentException(ERROR_DOMAIN_NULL);
        }

        ChartDataEntity entity = new ChartDataEntity();
        entity.setDate(domain.date());
        entity.setCost(domain.cost().getAmount());
        entity.setCpa(domain.cpa().getBigDecimal());
        entity.setRoi(domain.roi().getBigDecimal());
        entity.setConversions(domain.conversions());

        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChartDataEntity> toEntityList(List<ChartData> domains) {
        if (domains == null) {
            return List.of();
        }
        return domains.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    // ============================================================
    // ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ
    // ============================================================

    /**
     * Обновляет существующую Entity из Domain модели.
     *
     * <p>Используется при обновлении данных, сохраняет
     * id, version и timestamps.
     *
     * @param domain Domain модель с новыми данными
     * @param entity Существующая Entity для обновления
     * @return Обновленная Entity
     */
    public ChartDataEntity updateEntity(ChartData domain, ChartDataEntity entity) {
        if (domain == null || entity == null) {
            throw new IllegalArgumentException("Domain и Entity не могут быть null");
        }

        entity.setDate(domain.date());
        entity.setCost(domain.cost().getAmount());
        entity.setCpa(domain.cpa().getBigDecimal());
        entity.setRoi(domain.roi().getBigDecimal());
        entity.setConversions(domain.conversions());

        return entity;
    }

    // ============================================================
    // ПРИВАТНЫЕ МЕТОДЫ ДЛЯ ВОССТАНОВЛЕНИЯ ИЗ КЭША
    // ============================================================

    /**
     * Восстанавливает ChartData из Map (для кэша Redis).
     *
     * <p>Поддерживает различные форматы данных из кэша:
     * <ul>
     *   <li>LocalDate или String для даты</li>
     *   <li>BigDecimal, Double, Integer для чисел</li>
     *   <li>Вложенные объекты Money, Cpa, Roi</li>
     * </ul>
     *
     * @param map Map с данными из Redis
     * @return восстановленный ChartData
     * @throws IllegalArgumentException если данные невалидны
     */
    @SuppressWarnings("unchecked")
    private ChartData recoverFromMap(Map<?, ?> map) {
        try {
            // ✅ Извлечение и преобразование даты
            LocalDate date = extractDate(map.get("date"));

            // ✅ Извлечение числовых значений с поддержкой разных типов
            double cost = extractDouble(map, "cost");
            double cpa = extractDouble(map, "cpa");
            double roi = extractDouble(map, "roi");
            int conversions = extractInt(map, "conversions");

            log.debug("Восстановлены данные из кэша: date={}, cost={}, cpa={}, roi={}, conversions={}",
                    date, cost, cpa, roi, conversions);

            return new ChartData(
                    date,
                    Money.of(cost),
                    Cpa.of(cpa),
                    Roi.of(roi),
                    conversions
            );
        } catch (Exception e) {
            log.error("Ошибка восстановления ChartData из Map: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Не удалось восстановить ChartData из Map", e);
        }
    }

    /**
     * Извлекает LocalDate из объекта (поддерживает LocalDate и String).
     *
     * @param dateObj объект с датой
     * @return LocalDate
     * @throws IllegalArgumentException если дата невалидна
     */
    private LocalDate extractDate(Object dateObj) {
        if (dateObj == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        if (dateObj instanceof LocalDate) {
            return (LocalDate) dateObj;
        }

        if (dateObj instanceof String) {
            try {
                return LocalDate.parse((String) dateObj);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date format: " + dateObj, e);
            }
        }

        throw new IllegalArgumentException("Unsupported date type: " + dateObj.getClass().getName());
    }

    /**
     * Извлекает double из Map с поддержкой разных типов.
     *
     * @param map Map с данными
     * @param key ключ для извлечения
     * @return double значение
     * @throws IllegalArgumentException если значение не найдено или невалидно
     */
    private double extractDouble(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Key '" + key + "' not found in map");
        }

        // ✅ Поддержка вложенных объектов Value Objects
        if (value instanceof Money) {
            return ((Money) value).getValue();
        }
        if (value instanceof Cpa) {
            return ((Cpa) value).getValue();
        }
        if (value instanceof Roi) {
            return ((Roi) value).getValue();
        }

        // ✅ Поддержка примитивных типов
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        // ✅ Поддержка Map с вложенными полями
        if (value instanceof Map) {
            Map<?, ?> innerMap = (Map<?, ?>) value;

            // Пробуем получить из поля "value"
            Object innerValue = innerMap.get("value");
            if (innerValue instanceof Number) {
                return ((Number) innerValue).doubleValue();
            }

            // Для Money пробуем получить из поля "amount"
            Object amount = innerMap.get("amount");
            if (amount instanceof Number) {
                return ((Number) amount).doubleValue();
            }
        }

        throw new IllegalArgumentException("Cannot extract double from key '" + key +
                                           "', value type: " + value.getClass().getName());
    }

    /**
     * Извлекает int из Map с поддержкой разных типов.
     *
     * @param map Map с данными
     * @param key ключ для извлечения
     * @return int значение
     * @throws IllegalArgumentException если значение не найдено или невалидно
     */
    private int extractInt(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Key '" + key + "' not found in map");
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid integer format: " + value, e);
            }
        }

        throw new IllegalArgumentException("Cannot extract int from key '" + key +
                                           "', value type: " + value.getClass().getName());
    }
}