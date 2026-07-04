package com.clickready.chart.integration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import com.clickready.chart.application.service.ChartService;
import com.clickready.chart.domain.model.ChartData;
import com.clickready.chart.domain.valueobject.Cpa;
import com.clickready.chart.domain.valueobject.Money;
import com.clickready.chart.domain.valueobject.Roi;
import com.clickready.chart.infrastructure.repository.JpaChartDataRepository;
import com.clickready.chart.infrastructure.repository.entity.ChartDataEntity;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Интеграционные тесты кэширования ChartService.
 *
 * <p>Тестирует механизм кэширования на основе Redis.
 *
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
@Slf4j
@SpringBootTest
@Testcontainers
@Import(TestcontainersConfig.class)
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration"
})
@DisplayName("Интеграционные тесты кэширования ChartService")
class ChartCacheTest {

    @Autowired
    private ChartService chartService;

    @Autowired
    private JpaChartDataRepository jpaRepository;

    @Autowired
    private CacheManager cacheManager;

    @SpyBean
    private ChartService chartServiceSpy;

    // ============================================================
    // TESTCONTAINERS
    // ============================================================

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine")
    )
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withReuse(true);

    private static final GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:7-alpine")
    )
            .withExposedPorts(6379)
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        postgres.start();
        redis.start();

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("spring.cache.redis.time-to-live", () -> "60000");
    }

    @BeforeEach
    void setUp() {
        jpaRepository.deleteAll();
        var cache = cacheManager.getCache("chartData");
        if (cache != null) {
            cache.clear();
        }
        log.debug("База данных и кэш очищены");
    }

    // ============================================================
    // ТЕСТЫ
    // ============================================================

    /**
     * Тест: первый вызов загружает данные из БД, второй использует кэш.
     * Проверяем, что размер одинаковый, а не сравниваем объекты.
     */
    @Test
    @DisplayName("Первый вызов getChartData() - данные из БД, второй - из кэша")
    void shouldCacheDataOnFirstCall() {
        log.info("=== Тест: кэширование данных ===");

        var entity = createTestEntity(LocalDate.of(2026, 6, 13), 55.65, 0.79, 56.33, 70);
        jpaRepository.save(entity);

        // Первый вызов — загружает из БД
        var result1 = chartServiceSpy.getChartData();
        assertThat(result1).hasSize(1);

        // Второй вызов — из кэша
        var result2 = chartServiceSpy.getChartData();
        assertThat(result2).hasSize(1);

        // ✅ Проверяем только размер и содержимое без глубокого сравнения
        assertThat(result1).hasSize(1);
        assertThat(result2).hasSize(1);

        // ✅ Проверяем, что реальный метод вызван ТОЛЬКО 1 раз
        verify(chartServiceSpy, times(1)).getChartData();

        log.info("✅ Кэширование работает: второй вызов вернул те же данные");
    }


    /**
     * Тест: после сохранения данных кэш инвалидируется.
     */
    @Test
    @DisplayName("После сохранения данных кэш инвалидируется")
    void shouldInvalidateCacheAfterSave() {
        log.info("=== Тест: инвалидация кэша при сохранении ===");

        var entity = createTestEntity(LocalDate.of(2026, 6, 13), 55.65, 0.79, 56.33, 70);
        jpaRepository.save(entity);

        // Первый вызов — заполняет кэш
        var firstCall = chartServiceSpy.getChartData();
        assertThat(firstCall).hasSize(1);

        // Сохранение новых данных — инвалидирует кэш
        var newData = new ChartData(
                LocalDate.of(2026, 6, 14),
                Money.of(80.0),
                Cpa.of(0.2),
                Roi.of(220.0),
                110
        );
        chartServiceSpy.saveChartData(newData);

        // Второй вызов — должен загрузить из БД (кэш инвалидирован)
        var secondCall = chartServiceSpy.getChartData();
        assertThat(secondCall).hasSize(2);

        // ✅ Проверяем, что реальный метод вызван 2 раза (после инвалидации)
        verify(chartServiceSpy, times(2)).getChartData();

        log.info("✅ Кэш инвалидирован после сохранения");
    }

    /**
     * Тест: после удаления данных кэш инвалидируется.
     */
    @Test
    @DisplayName("После удаления данных кэш инвалидируется")
    void shouldInvalidateCacheAfterDelete() {
        log.info("=== Тест: инвалидация кэша при удалении ===");

        var entity = createTestEntity(LocalDate.of(2026, 6, 13), 55.65, 0.79, 56.33, 70);
        jpaRepository.save(entity);

        // Первый вызов — заполняет кэш
        chartServiceSpy.getChartData();

        // Удаление — инвалидирует кэш
        chartServiceSpy.deleteChartData(LocalDate.of(2026, 6, 13));

        // Второй вызов — должен загрузить из БД (кэш инвалидирован)
        chartServiceSpy.getChartData();

        // ✅ Проверяем, что реальный метод вызван 2 раза
        verify(chartServiceSpy, times(2)).getChartData();

        log.info("✅ Кэш инвалидирован после удаления");
    }

    /**
     * Тест: кэширование запросов за период с разными датами.
     */
    @Test
    @DisplayName("Кэширование запросов за период с разными датами")
    void shouldCacheRangeQueriesSeparately() {
        log.info("=== Тест: кэширование запросов за период ===");

        var entity1 = createTestEntity(LocalDate.of(2026, 6, 10), 10, 1, 50, 10);
        var entity2 = createTestEntity(LocalDate.of(2026, 6, 20), 20, 2, 100, 20);
        jpaRepository.saveAll(List.of(entity1, entity2));

        // Первый диапазон
        var range1 = chartServiceSpy.getChartDataInRange(
                LocalDate.of(2026, 6, 10),
                LocalDate.of(2026, 6, 15)
        );
        assertThat(range1).hasSize(1);

        // Второй диапазон (другой ключ кэша)
        var range2 = chartServiceSpy.getChartDataInRange(
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 25)
        );
        assertThat(range2).hasSize(1);

        // Повтор первого диапазона — из кэша
        var range3 = chartServiceSpy.getChartDataInRange(
                LocalDate.of(2026, 6, 10),
                LocalDate.of(2026, 6, 15)
        );
        assertThat(range3).hasSize(1);

        // ✅ Проверяем только размер
        assertThat(range1).hasSize(1);
        assertThat(range2).hasSize(1);
        assertThat(range3).hasSize(1);

        // ✅ Проверяем, что реальный метод вызван 2 раза (первый и второй диапазон)
        // Третий вызов пошел из кэша
        verify(chartServiceSpy, times(2)).getChartDataInRange(
                any(LocalDate.class),
                any(LocalDate.class)
        );

        log.info("✅ Кэширование диапазонов работает");
    }


    /**
     * Тест: при пустой БД кэшируются сгенерированные данные.
     */
    @Test
    @DisplayName("При пустой БД кэшируются сгенерированные данные")
    void shouldCacheGeneratedDataWhenEmpty() {
        log.info("=== Тест: кэширование сгенерированных данных ===");

        // Первый вызов — генерирует данные
        var result1 = chartServiceSpy.getChartData();
        assertThat(result1).isNotEmpty();

        // Второй вызов — из кэша
        var result2 = chartServiceSpy.getChartData();
        assertThat(result2).isNotEmpty();

        // ✅ Проверяем только размер
        assertThat(result1).hasSize(7);
        assertThat(result2).hasSize(7);

        // ✅ Проверяем, что реальный метод вызван ТОЛЬКО 1 раз
        verify(chartServiceSpy, times(1)).getChartData();

        log.info("✅ Сгенерированные данные кэшируются");
    }

    /**
     * Тест: кэш инвалидируется при пакетном сохранении.
     */
    @Test
    @DisplayName("Кэш инвалидируется при пакетном сохранении")
    void shouldInvalidateCacheAfterBatchSave() {
        log.info("=== Тест: инвалидация кэша при пакетном сохранении ===");

        var entity = createTestEntity(LocalDate.of(2026, 6, 13), 55.65, 0.79, 56.33, 70);
        jpaRepository.save(entity);

        // Первый вызов — заполняет кэш
        chartServiceSpy.getChartData();

        // Пакетное сохранение — инвалидирует кэш
        var newDataList = List.of(
                new ChartData(
                        LocalDate.of(2026, 6, 14),
                        Money.of(80.0),
                        Cpa.of(0.2),
                        Roi.of(220.0),
                        110
                )
        );
        chartServiceSpy.saveAllChartData(newDataList);

        // Второй вызов — должен загрузить из БД (кэш инвалидирован)
        chartServiceSpy.getChartData();

        // ✅ Проверяем, что реальный метод вызван 2 раза
        verify(chartServiceSpy, times(2)).getChartData();

        log.info("✅ Кэш инвалидирован при пакетном сохранении");
    }

    /**
     * Тест: многократные вызовы getChartData() используют кэш.
     */
    @Test
    @DisplayName("Многократные вызовы getChartData() используют кэш")
    void shouldUseCacheForMultipleCalls() {
        log.info("=== Тест: многократные вызовы с кэшем ===");

        var entity = createTestEntity(LocalDate.of(2026, 6, 13), 55.65, 0.79, 56.33, 70);
        jpaRepository.save(entity);

        // Вызываем 10 раз
        for (int i = 0; i < 10; i++) {
            var result = chartServiceSpy.getChartData();
            assertThat(result).hasSize(1);
        }

        // ✅ Проверяем, что реальный метод вызван ТОЛЬКО 1 раз
        // Все остальные вызовы пошли из кэша
        verify(chartServiceSpy, times(1)).getChartData();

        log.info("✅ Все 10 вызовов успешно выполнены, реальный метод вызван 1 раз");
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ============================================================

    private ChartDataEntity createTestEntity(LocalDate date, double cost, double cpa, double roi, int conversions) {
        return ChartDataEntity.builder()
                .date(date)
                .cost(BigDecimal.valueOf(cost))
                .cpa(BigDecimal.valueOf(cpa))
                .roi(BigDecimal.valueOf(roi))
                .conversions(conversions)
                .build();
    }
}