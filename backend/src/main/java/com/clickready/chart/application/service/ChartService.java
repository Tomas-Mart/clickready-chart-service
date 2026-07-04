package com.clickready.chart.application.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.clickready.chart.application.port.in.GetChartDataUseCase;
import com.clickready.chart.application.port.in.SaveChartDataUseCase;
import com.clickready.chart.application.port.in.UpdateChartDataUseCase;
import com.clickready.chart.application.port.out.ChartRepositoryPort;
import com.clickready.chart.application.port.out.EventPublisherPort;
import com.clickready.chart.domain.event.ChartDataUpdatedEvent;
import com.clickready.chart.domain.model.ChartData;
import com.clickready.chart.domain.valueobject.DateRange;
import com.clickready.chart.presentation.exception.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Сервис для работы с данными графика.
 * Реализует Use Cases и бизнес-логику с поддержкой кэширования.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChartService implements
        GetChartDataUseCase,
        SaveChartDataUseCase,
        UpdateChartDataUseCase {

    private final ChartRepositoryPort repository;
    private final ChartDataGenerator dataGenerator;
    private final EventPublisherPort eventPublisher;

    /**
     * Получить все данные графика.
     * Результат кэшируется в Redis.
     *
     * @return список данных
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "chartData", key = "'all'")
    public List<ChartData> getChartData() {
        log.debug("Выполняется запрос всех данных графика (БД)");

        var data = repository.findAll();

        if (data.isEmpty()) {
            log.warn("Данные графика не найдены в БД, генерируем тестовые");
            return dataGenerator.generateDefaultData();
        }

        log.info("Найдено {} записей данных графика", data.size());
        return data;
    }

    /**
     * Получить данные за период.
     * Результат кэшируется в Redis с ключом по датам.
     *
     * @param startDate начальная дата
     * @param endDate   конечная дата
     * @return список данных за период
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "chartData", key = "#startDate.toString() + '-' + #endDate.toString()")
    public List<ChartData> getChartDataInRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Запрос данных за период с {} по {} (БД)", startDate, endDate);

        var dateRange = new DateRange(startDate, endDate);
        var data = repository.findByDateRange(dateRange);

        log.info("Найдено {} записей за указанный период", data.size());
        return data;
    }

    /**
     * Сохранить новые данные.
     * Инвалидирует кэш после сохранения.
     *
     * @param data данные для сохранения
     * @return сохраненные данные
     */
    @Override
    @Transactional
    @CacheEvict(value = "chartData", allEntries = true)
    public ChartData saveChartData(ChartData data) {
        log.info("Сохранение новых данных графика: {}", data);

        // ✅ Валидация в сервисе
        validateChartData(data);

        var savedData = repository.save(data);
        log.info("Данные успешно сохранены: {}", savedData);

        var event = new ChartDataUpdatedEvent(savedData);
        eventPublisher.publish(event);

        return savedData;
    }

    /**
     * Пакетное сохранение данных.
     * Инвалидирует кэш после сохранения.
     *
     * @param dataList список данных для сохранения
     * @return сохраненные данные
     */
    @Override
    @Transactional
    @CacheEvict(value = "chartData", allEntries = true)
    public List<ChartData> saveAllChartData(List<ChartData> dataList) {
        log.info("Пакетное сохранение {} записей", dataList.size());

        // ✅ Валидация всех записей в сервисе
        var invalidData = dataList.stream()
                .filter(d -> !isValid(d))
                .toList();

        if (!invalidData.isEmpty()) {
            var error = "Найдены невалидные записи: " + invalidData.size();
            log.error(error);
            throw new IllegalArgumentException(error + ". Проверьте данные.");
        }

        var savedData = repository.saveAll(dataList);
        log.info("Пакетно сохранено {} записей", savedData.size());

        savedData.forEach(data -> {
            var event = new ChartDataUpdatedEvent(data);
            eventPublisher.publish(event);
        });

        return savedData;
    }

    /**
     * Удалить данные за дату.
     * Инвалидирует кэш после удаления.
     *
     * @param date дата для удаления
     */
    @Override
    @Transactional
    @CacheEvict(value = "chartData", allEntries = true)
    public void deleteChartData(LocalDate date) {
        log.info("Удаление данных за дату: {}", date);

        var deleted = repository.deleteByDate(date);

        if (!deleted) {
            log.warn("Данные за {} не найдены для удаления", date);
            throw new DataNotFoundException(
                    "ChartData",
                    "date",
                    date,
                    "Данные за " + date + " не найдены"
            );
        }

        log.info("Данные за {} успешно удалены", date);
    }

    // ============================================================
    // PRIVATE METHODS
    // ============================================================

    /**
     * Валидация данных графика.
     *
     * @param data данные для проверки
     * @throws IllegalArgumentException если данные невалидны
     */
    private void validateChartData(ChartData data) {
        if (!isValid(data)) {
            var error = "Невалидные данные графика: " + data;
            log.error(error);
            throw new IllegalArgumentException(error);
        }
    }

    /**
     * Проверяет валидность данных.
     *
     * @param data данные для проверки
     * @return true если данные валидны
     */
    private boolean isValid(ChartData data) {
        return data != null && data.isValid();
    }
}