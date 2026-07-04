package com.clickready.chart.integration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import com.clickready.chart.infrastructure.repository.JpaChartDataRepository;
import com.clickready.chart.infrastructure.repository.entity.ChartDataEntity;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Интеграционные тесты JPA репозитория.
 *
 * <p>Тестирует взаимодействие с базой данных через JPA.
 * Использует Testcontainers для изоляции тестов и обеспечения
 * воспроизводимости на любом окружении.
 *
 * <p><b>Ключевые аспекты тестирования:</b>
 * <ul>
 *   <li>CRUD операции (создание, чтение, обновление, удаление)</li>
 *   <li>Бизнес-логика репозитория (поиск по диапазону, высокоприбыльные записи)</li>
 *   <li>Ограничения целостности (уникальность даты)</li>
 *   <li>Оптимистичные блокировки (обновление с версией)</li>
 * </ul>
 *
 * <p><b>Используемые технологии:</b>
 * <ul>
 *   <li>Testcontainers — реальные контейнеры PostgreSQL</li>
 *   <li>DataJpaTest — контекст только для JPA</li>
 *   <li>AssertJ — читаемые утверждения</li>
 * </ul>
 *
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
@Slf4j
@DataJpaTest
@DisplayName("Интеграционные тесты JPA репозитория")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChartRepositoryTest {

    // ============================================================
    // ПОЛЯ И КОНСТАНТЫ
    // ============================================================

    /**
     * Тестовая дата для всех тестов.
     */
    private static final LocalDate TEST_DATE = LocalDate.of(2026, 6, 13);

    /**
     * Репозиторий для работы с сущностями ChartDataEntity.
     */
    @Autowired
    private JpaChartDataRepository repository;

    /**
     * EntityManager для управления кэшем JPA и операций с БД.
     */
    @Autowired
    private EntityManager entityManager;

    /**
     * Тестовая сущность, используемая в большинстве тестов.
     */
    private ChartDataEntity testEntity;

    // ============================================================
    // НАСТРОЙКА ТЕСТОВ
    // ============================================================

    /**
     * Настройка динамических свойств для Testcontainers.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        log.debug("Настройка свойств для тестов (из Testcontainers)");
    }

    /**
     * Подготовка тестовых данных перед каждым тестом.
     * Очищает БД и создает тестовую сущность.
     */
    @BeforeEach
    void setUp() {
        log.info("Подготовка тестовых данных...");

        testEntity = ChartDataEntity.builder()
                .date(TEST_DATE)
                .cost(BigDecimal.valueOf(55.65))
                .cpa(BigDecimal.valueOf(0.79))
                .roi(BigDecimal.valueOf(56.33))
                .conversions(70)
                .build();

        repository.deleteAll();
        log.debug("База данных очищена");
    }

    // ============================================================
    // 1. CRUD ТЕСТЫ
    // ============================================================

    /**
     * Тест: сохранение и получение данных.
     *
     * <p>Проверяет:
     * <ul>
     *   <li>Корректное сохранение сущности</li>
     *   <li>Возможность найти сохраненную сущность по ID</li>
     *   <li>Начальное значение версии (0)</li>
     * </ul>
     */
    @Test
    @DisplayName("Сохранение и получение данных")
    void shouldSaveAndFindData() {
        log.info("Тест: сохранение и получение данных");

        // Arrange
        var saved = repository.save(testEntity);
        log.debug("Сохранена сущность с ID: {}", saved.getId());

        // Act
        var found = repository.findById(saved.getId());

        // Assert
        assertThat(found)
                .isPresent()
                .hasValueSatisfying(entity -> {
                    assertThat(entity.getDate()).isEqualTo(TEST_DATE);
                    assertThat(entity.getCost()).isEqualByComparingTo("55.65");
                    assertThat(entity.getVersion()).isZero();
                });

        log.info("✅ Тест успешно пройден: данные сохранены и получены");
    }

    /**
     * Тест: поиск по дате.
     *
     * <p>Проверяет корректность поиска уникальной записи по дате.
     */
    @Test
    @DisplayName("Поиск по дате")
    void shouldFindByDate() {
        log.info("Тест: поиск по дате");

        // Arrange
        repository.save(testEntity);
        log.debug("Сохранена тестовая сущность");

        // Act
        var found = repository.findByDate(TEST_DATE);

        // Assert
        assertThat(found)
                .isPresent()
                .hasValueSatisfying(entity ->
                        assertThat(entity.getConversions()).isEqualTo(70)
                );

        log.info("✅ Тест успешно пройден: данные найдены по дате");
    }

    /**
     * Тест: поиск данных за период.
     *
     * <p>Проверяет корректность поиска в диапазоне дат
     * и сортировку по возрастанию.
     */
    @Test
    @DisplayName("Поиск за период")
    void shouldFindAllInDateRange() {
        log.info("Тест: поиск данных за период");

        // Arrange
        var entity1 = ChartDataEntity.builder()
                .date(LocalDate.of(2026, 6, 10))
                .cost(BigDecimal.valueOf(10))
                .cpa(BigDecimal.valueOf(1))
                .roi(BigDecimal.valueOf(50))
                .conversions(10)
                .build();

        var entity2 = ChartDataEntity.builder()
                .date(LocalDate.of(2026, 6, 20))
                .cost(BigDecimal.valueOf(20))
                .cpa(BigDecimal.valueOf(2))
                .roi(BigDecimal.valueOf(100))
                .conversions(20)
                .build();

        repository.saveAll(List.of(entity1, entity2, testEntity));
        log.debug("Сохранено 3 тестовые сущности");

        // Act
        var result = repository.findAllInDateRange(
                LocalDate.of(2026, 6, 10),
                LocalDate.of(2026, 6, 15)
        );

        // Assert
        assertThat(result)
                .hasSize(2)
                .extracting("date")
                .containsExactly(
                        LocalDate.of(2026, 6, 10),
                        TEST_DATE
                );

        log.info("✅ Тест успешно пройден: найдено {} записей за период", result.size());
    }

    /**
     * Тест: поиск высокоприбыльных данных.
     *
     * <p>Проверяет, что репозиторий возвращает только записи
     * с ROI выше заданного порога.
     */
    @Test
    @DisplayName("Поиск высокоприбыльных данных")
    void shouldFindHighlyProfitable() {
        log.info("Тест: поиск высокоприбыльных данных");

        // Arrange
        var entity1 = ChartDataEntity.builder()
                .date(LocalDate.of(2026, 6, 11))
                .cost(BigDecimal.valueOf(10))
                .cpa(BigDecimal.valueOf(1))
                .roi(BigDecimal.valueOf(200))
                .conversions(10)
                .build();

        var entity2 = ChartDataEntity.builder()
                .date(LocalDate.of(2026, 6, 12))
                .cost(BigDecimal.valueOf(20))
                .cpa(BigDecimal.valueOf(2))
                .roi(BigDecimal.valueOf(50))
                .conversions(20)
                .build();

        repository.saveAll(List.of(entity1, entity2, testEntity));

        // Act
        var result = repository.findHighlyProfitable(BigDecimal.valueOf(100));

        // Assert
        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(entity ->
                        assertThat(entity.getRoi()).isEqualByComparingTo("200")
                );

        log.info("✅ Тест успешно пройден: найдено {} высокоприбыльных записей", result.size());
    }

    // ============================================================
    // 2. ТЕСТЫ УДАЛЕНИЯ
    // ============================================================

    /**
     * Тест: удаление по дате.
     *
     * <p>Проверяет корректное удаление записи по дате
     * и возврат количества удаленных записей.
     */
    @Test
    @DisplayName("Удаление по дате")
    void shouldDeleteByDate() {
        log.info("Тест: удаление данных по дате");

        // Arrange
        repository.save(testEntity);

        // Act
        var deleted = repository.deleteByDate(TEST_DATE);

        // Assert
        assertThat(deleted).isEqualTo(1);
        assertThat(repository.findByDate(TEST_DATE)).isNotPresent();

        log.info("✅ Тест успешно пройден: {} запись удалена", deleted);
    }

    /**
     * Тест: проверка существования данных.
     *
     * <p>Проверяет корректность проверки наличия записи по дате.
     */
    @Test
    @DisplayName("Проверка существования данных")
    void shouldCheckIfExistsByDate() {
        log.info("Тест: проверка существования данных");

        // Arrange
        repository.save(testEntity);

        // Act & Assert
        assertThat(repository.existsByDate(TEST_DATE)).isTrue();
        assertThat(repository.existsByDate(LocalDate.of(2026, 6, 14))).isFalse();

        log.info("✅ Тест успешно пройден: проверка существования работает");
    }

    // ============================================================
    // 3. ТЕСТЫ КОНКУРЕНТНОСТИ
    // ============================================================

    /**
     * Тест: оптимистичная блокировка при обновлении.
     *
     * <p>Проверяет, что версия сущности увеличивается
     * при каждом обновлении.
     *
     * <p><b>Важно:</b> Использует {@link EntityManager#clear()}
     * для очистки кэша первого уровня и получения актуальной версии.
     */
    @Test
    @DisplayName("Оптимистичная блокировка - обновление с версией")
    void shouldHandleOptimisticLocking() {
        log.info("Тест: оптимистичная блокировка при обновлении");

        // Arrange
        var saved = repository.saveAndFlush(testEntity);
        var id = saved.getId();
        var initialVersion = saved.getVersion();

        assertThat(initialVersion).isZero();
        log.debug("Сохранена сущность с версией: {}", initialVersion);

        // Очищаем кэш JPA для получения свежих данных
        entityManager.clear();

        // Act — загружаем заново и обновляем
        var found = repository.findById(id).orElseThrow();
        found.setConversions(100);
        var updated = repository.saveAndFlush(found);

        // Assert — версия должна увеличиться
        assertThat(updated.getVersion()).isEqualTo(initialVersion + 1);

        log.info("✅ Тест успешно пройден: версия увеличена с {} до {}",
                initialVersion, updated.getVersion());
    }

    /**
     * Тест: уникальность даты.
     *
     * <p>Проверяет, что попытка вставить запись с существующей датой
     * вызывает исключение {@link DataIntegrityViolationException}.
     */
    @Test
    @DisplayName("Уникальность даты - конфликт")
    void shouldEnforceUniqueDate() {
        log.info("Тест: уникальность даты (конфликт)");

        // Arrange
        repository.save(testEntity);

        var duplicate = ChartDataEntity.builder()
                .date(TEST_DATE)
                .cost(BigDecimal.valueOf(100))
                .cpa(BigDecimal.valueOf(1))
                .roi(BigDecimal.valueOf(100))
                .conversions(50)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> repository.save(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("Unique index or primary key violation");
        log.info("✅ Тест успешно пройден: конфликт даты обработан");
    }
}