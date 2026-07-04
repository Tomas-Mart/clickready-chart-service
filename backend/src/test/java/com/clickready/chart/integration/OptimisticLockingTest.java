package com.clickready.chart.integration;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.clickready.chart.infrastructure.repository.JpaChartDataRepository;
import com.clickready.chart.infrastructure.repository.entity.ChartDataEntity;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Интеграционные тесты оптимистичной блокировки.
 *
 * <p>Тестирует механизм оптимистичных блокировок Hibernate
 * при конкурентном доступе к данным.
 *
 * <p><b>Ключевые аспекты тестирования:</b>
 * <ul>
 *   <li>Конфликт при одновременном обновлении одной записи</li>
 *   <li>Параллельные транзакции с устаревшей версией</li>
 *   <li>Использование TransactionTemplate для управления транзакциями</li>
 * </ul>
 *
 * <p><b>Используемые технологии:</b>
 * <ul>
 *   <li>Testcontainers — реальные контейнеры PostgreSQL</li>
 *   <li>SpringBootTest — полный контекст приложения</li>
 *   <li>TransactionTemplate — программное управление транзакциями</li>
 *   <li>AssertJ — читаемые утверждения</li>
 * </ul>
 *
 * <p><b>Почему отдельный класс?</b>
 * <ul>
 *   <li>Оптимистичные блокировки требуют отдельных транзакций</li>
 *   <li>DataJpaTest не поддерживает изоляцию транзакций</li>
 *   <li>TransactionTemplate дает полный контроль над транзакциями</li>
 * </ul>
 *
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
@Slf4j
@SpringBootTest
@Testcontainers
@Import(TestcontainersConfig.class)
@DisplayName("Тесты оптимистичной блокировки")
class OptimisticLockingTest {

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
     * TransactionTemplate для программного управления транзакциями.
     * Позволяет выполнять операции в отдельных транзакциях.
     */
    @Autowired
    private TransactionTemplate transactionTemplate;

    // ============================================================
    // НАСТРОЙКА
    // ============================================================

    /**
     * Очистка базы данных перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        repository.deleteAll();
        log.debug("База данных очищена");
    }

    // ============================================================
    // ТЕСТЫ
    // ============================================================

    /**
     * Тест: конфликт оптимистичной блокировки при одновременном обновлении.
     *
     * <p><b>Сценарий:</b>
     * <ol>
     *   <li>Создание сущности с версией 0 в отдельной транзакции</li>
     *   <li>Загрузка двух экземпляров с одинаковой версией 0</li>
     *   <li>Первая транзакция обновляет entity1 → версия становится 1</li>
     *   <li>Вторая транзакция пытается обновить entity2 с устаревшей версией 0</li>
     *   <li>Ожидаем {@link ObjectOptimisticLockingFailureException}</li>
     * </ol>
     *
     * <p><b>Важно:</b> Экземпляры загружаются до начала транзакций,
     * чтобы оба имели одинаковую версию. Это имитирует реальный сценарий,
     * когда два пользователя одновременно загрузили и пытаются обновить
     * одну и ту же запись.
     *
     * <p><b>Ожидаемый результат:</b> Исключение ObjectOptimisticLockingFailureException
     * с сообщением о том, что запись была обновлена другой транзакцией.
     */
    @Test
    @DisplayName("Оптимистичная блокировка - конфликт при одновременном обновлении")
    void shouldThrowOptimisticLockException() {
        log.info("Тест: конфликт оптимистичной блокировки");

        // Шаг 1: Создаём сущность в отдельной транзакции
        var saved = transactionTemplate.execute(status -> {
            var entity = new ChartDataEntity();
            entity.setDate(TEST_DATE);
            entity.setCost(BigDecimal.valueOf(55.65));
            entity.setCpa(BigDecimal.valueOf(0.79));
            entity.setRoi(BigDecimal.valueOf(56.33));
            entity.setConversions(70);
            entity.setVersion(0);
            return repository.saveAndFlush(entity);
        });

        var id = saved.getId();
        log.debug("Сохранена сущность с ID: {}, версия: {}", id, saved.getVersion());

        // Шаг 2: Загружаем два экземпляра ДО первого обновления
        // Оба имеют версию 0 — симулируем двух пользователей
        var entity1 = repository.findById(id).orElseThrow();
        var entity2 = repository.findById(id).orElseThrow();

        log.debug("Entity1 версия: {}, Entity2 версия: {}",
                entity1.getVersion(), entity2.getVersion());

        // Шаг 3: Первый пользователь успешно обновляет запись
        // Версия становится 1
        transactionTemplate.execute(status -> {
            entity1.setConversions(100);
            repository.saveAndFlush(entity1);
            log.debug("Entity1 обновлён, новая версия: {}", entity1.getVersion());
            return null;
        });

        // Шаг 4: Второй пользователь пытается обновить с устаревшей версией 0
        // Должно выбросить исключение
        assertThatThrownBy(() -> transactionTemplate.execute(status -> {
            entity2.setConversions(200);
            repository.saveAndFlush(entity2);
            return null;
        }))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class)
                .hasMessageContaining("Row was updated or deleted by another transaction");

        log.info("✅ Тест успешно пройден: конфликт блокировки обработан");
    }

    /**
     * Тест: два параллельных обновления с одинаковой версией.
     *
     * <p><b>Сценарий:</b>
     * <ol>
     *   <li>Создание сущности с версией 0</li>
     *   <li>Загрузка двух экземпляров с одинаковой версией 0</li>
     *   <li>Первая транзакция обновляет entity1 → версия становится 1</li>
     *   <li>Вторая транзакция пытается обновить entity2 с версией 0</li>
     *   <li>Ожидаем {@link ObjectOptimisticLockingFailureException}</li>
     * </ol>
     *
     * <p><b>Отличие от первого теста:</b> Используется другая дата
     * для изоляции тестов и проверки, что блокировка работает
     * для любой записи.
     *
     * <p><b>Ожидаемый результат:</b> Исключение ObjectOptimisticLockingFailureException
     * с сообщением о конфликте версий.
     */
    @Test
    @DisplayName("Оптимистичная блокировка - два параллельных обновления")
    void shouldThrowOptimisticLockExceptionWithTwoTransactions() {
        log.info("Тест: два параллельных обновления");

        // Шаг 1: Создаём сущность в отдельной транзакции
        var saved = transactionTemplate.execute(status -> {
            var entity = new ChartDataEntity();
            entity.setDate(TEST_DATE.plusDays(1));
            entity.setCost(BigDecimal.valueOf(100));
            entity.setCpa(BigDecimal.valueOf(1));
            entity.setRoi(BigDecimal.valueOf(50));
            entity.setConversions(50);
            entity.setVersion(0);
            return repository.saveAndFlush(entity);
        });

        var id = saved.getId();
        log.debug("Сохранена сущность с ID: {}", id);

        // Шаг 2: Загружаем два экземпляра с одинаковой версией 0
        // НЕ очищаем кэш, чтобы получить два экземпляра с одинаковой версией
        var entity1 = repository.findById(id).orElseThrow();
        var entity2 = repository.findById(id).orElseThrow();

        log.debug("Entity1 версия: {}, Entity2 версия: {}",
                entity1.getVersion(), entity2.getVersion());

        // Шаг 3: Первое обновление успешно
        transactionTemplate.execute(status -> {
            entity1.setConversions(150);
            repository.saveAndFlush(entity1);
            log.debug("Entity1 обновлён, новая версия: {}", entity1.getVersion());
            return null;
        });

        // Шаг 4: Второе обновление с устаревшей версией — исключение
        assertThatThrownBy(() -> transactionTemplate.execute(status -> {
            entity2.setConversions(200);
            repository.saveAndFlush(entity2);
            return null;
        }))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class)
                .hasMessageContaining("Row was updated or deleted by another transaction");

        log.info("✅ Тест успешно пройден: конфликт блокировки обработан");
    }
}