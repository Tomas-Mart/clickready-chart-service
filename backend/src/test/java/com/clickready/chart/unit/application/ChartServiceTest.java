package com.clickready.chart.unit.application;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.clickready.chart.application.port.out.ChartRepositoryPort;
import com.clickready.chart.application.port.out.EventPublisherPort;
import com.clickready.chart.application.service.ChartDataGenerator;
import com.clickready.chart.application.service.ChartService;
import com.clickready.chart.domain.model.ChartData;
import com.clickready.chart.domain.valueobject.Cpa;
import com.clickready.chart.domain.valueobject.Money;
import com.clickready.chart.domain.valueobject.Roi;
import com.clickready.chart.presentation.exception.DataNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса ChartService")
class ChartServiceTest {

    @Mock
    private ChartRepositoryPort repository;

    @Mock
    private ChartDataGenerator dataGenerator;

    @Mock
    private EventPublisherPort eventPublisher;

    @InjectMocks
    private ChartService chartService;

    private ChartData testData;
    private static final LocalDate TEST_DATE = LocalDate.of(2026, 6, 13);
    private static final double TEST_COST = 55.65;
    private static final double TEST_CPA = 0.79;
    private static final double TEST_ROI = 56.33;
    private static final int TEST_CONVERSIONS = 70;

    @BeforeEach
    void setUp() {
        testData = new ChartData(
                TEST_DATE,
                Money.of(TEST_COST),
                Cpa.of(TEST_CPA),
                Roi.of(TEST_ROI),
                TEST_CONVERSIONS
        );
    }

    @Test
    @DisplayName("Получение всех данных - успешный сценарий")
    void shouldGetAllChartData() {
        var mockData = List.of(testData);
        when(repository.findAll()).thenReturn(mockData);

        var result = chartService.getChartData();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().conversions()).isEqualTo(TEST_CONVERSIONS);
        verify(repository).findAll();
        verify(dataGenerator, never()).generateDefaultData();
    }

    @Test
    @DisplayName("Получение данных - пустая БД, генерация заглушки")
    void shouldGenerateDefaultDataWhenRepositoryEmpty() {
        when(repository.findAll()).thenReturn(List.of());
        var defaultData = List.of(testData);
        when(dataGenerator.generateDefaultData()).thenReturn(defaultData);

        var result = chartService.getChartData();

        assertThat(result).isEqualTo(defaultData);
        verify(dataGenerator).generateDefaultData();
    }

    @Test
    @DisplayName("Сохранение новых данных - успешный сценарий")
    void shouldSaveChartData() {
        when(repository.save(any(ChartData.class))).thenReturn(testData);

        var result = chartService.saveChartData(testData);

        assertThat(result).isEqualTo(testData);
        verify(repository).save(testData);
        verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("Сохранение невалидных данных - выбрасывание исключения")
    void shouldThrowExceptionForInvalidData() {
        var invalidData = new ChartData(
                null,
                Money.of(TEST_COST),
                Cpa.of(TEST_CPA),
                Roi.of(TEST_ROI),
                TEST_CONVERSIONS
        );

        assertThatThrownBy(() -> chartService.saveChartData(invalidData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Невалидные данные графика");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Сохранение данных с отрицательными конверсиями - исключение")
    void shouldThrowExceptionForNegativeConversions() {
        var invalidData = new ChartData(
                TEST_DATE,
                Money.of(TEST_COST),
                Cpa.of(TEST_CPA),
                Roi.of(TEST_ROI),
                -5
        );

        assertThatThrownBy(() -> chartService.saveChartData(invalidData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Невалидные данные графика");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Пакетное сохранение данных - успешный сценарий")
    void shouldSaveAllChartData() {
        var dataList = List.of(testData, testData);
        when(repository.saveAll(anyList())).thenReturn(dataList);

        var result = chartService.saveAllChartData(dataList);

        assertThat(result).hasSize(2);
        verify(repository).saveAll(dataList);
        verify(eventPublisher, times(2)).publish(any());
    }

    @Test
    @DisplayName("Пакетное сохранение с невалидными данными - исключение")
    void shouldThrowExceptionWhenBatchHasInvalidData() {
        var invalidData = new ChartData(
                null,
                Money.of(TEST_COST),
                Cpa.of(TEST_CPA),
                Roi.of(TEST_ROI),
                TEST_CONVERSIONS
        );
        var dataList = List.of(testData, invalidData);

        assertThatThrownBy(() -> chartService.saveAllChartData(dataList))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Найдены невалидные записи");

        verify(repository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Удаление данных - успешный сценарий")
    void shouldDeleteChartData() {
        when(repository.deleteByDate(TEST_DATE)).thenReturn(true);

        chartService.deleteChartData(TEST_DATE);

        verify(repository).deleteByDate(TEST_DATE);
    }

    @Test
    @DisplayName("Удаление несуществующих данных - выбрасывание исключения")
    void shouldThrowExceptionWhenDeletingNonExistentData() {
        LocalDate testDate = LocalDate.of(2026, 6, 13);
        when(repository.deleteByDate(testDate)).thenReturn(false);

        assertThatThrownBy(() -> chartService.deleteChartData(testDate))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessageContaining("ChartData с date='2026-06-13' не найден");
    }

    @Test
    @DisplayName("Получение данных за период")
    void shouldGetChartDataInRange() {
        var startDate = LocalDate.of(2026, 6, 1);
        var endDate = LocalDate.of(2026, 6, 30);
        var mockData = List.of(testData);

        when(repository.findByDateRange(any())).thenReturn(mockData);

        var result = chartService.getChartDataInRange(startDate, endDate);

        assertThat(result).hasSize(1);
        verify(repository).findByDateRange(any());
    }
}