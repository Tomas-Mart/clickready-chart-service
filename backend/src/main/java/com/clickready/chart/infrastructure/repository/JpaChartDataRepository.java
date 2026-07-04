package com.clickready.chart.infrastructure.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.clickready.chart.infrastructure.repository.entity.ChartDataEntity;

/**
 * JPA репозиторий для работы с сущностью ChartData.
 */
@Repository
public interface JpaChartDataRepository extends JpaRepository<ChartDataEntity, Long> {

    /**
     * Находит данные за указанную дату.
     *
     * @param date дата
     * @return данные за дату
     */
    Optional<ChartDataEntity> findByDate(LocalDate date);

    /**
     * Находит данные за период.
     *
     * @param startDate начальная дата
     * @param endDate   конечная дата
     * @return список данных
     */
    @Query("SELECT c FROM ChartDataEntity c WHERE c.date BETWEEN :startDate AND :endDate ORDER BY c.date ASC")
    List<ChartDataEntity> findAllInDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Находит данные с высокой прибыльностью.
     *
     * @param threshold порог ROI
     * @return список данных
     */
    @Query("""
             SELECT c FROM ChartDataEntity c\s
             WHERE c.roi >= :threshold\s
             ORDER BY c.roi DESC
            \s""")
    List<ChartDataEntity> findHighlyProfitable(@Param("threshold") BigDecimal threshold);

    /**
     * Удаляет данные за дату.
     *
     * @param date дата
     * @return количество удаленных записей
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ChartDataEntity c WHERE c.date = :date")
    int deleteByDate(@Param("date") LocalDate date);

    /**
     * Проверяет существование данных за дату.
     *
     * @param date дата
     * @return true если данные существуют
     */
    boolean existsByDate(LocalDate date);
}