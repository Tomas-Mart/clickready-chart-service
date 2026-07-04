package com.clickready.chart.infrastructure.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.clickready.chart.application.port.out.ChartRepositoryPort;
import com.clickready.chart.domain.model.ChartData;
import com.clickready.chart.domain.valueobject.Cpa;
import com.clickready.chart.domain.valueobject.DateRange;
import com.clickready.chart.domain.valueobject.Money;
import com.clickready.chart.domain.valueobject.Roi;
import com.clickready.chart.infrastructure.repository.entity.ChartDataEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ChartDataRepositoryImpl implements ChartRepositoryPort {

    private final JpaChartDataRepository jpaRepository;

    @Override
    public List<ChartData> findAll() {
        log.debug("Поиск всех данных графика");
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<ChartData> findByDate(LocalDate date) {
        log.debug("Поиск данных за дату: {}", date);
        return jpaRepository.findByDate(date)
                .map(this::toDomain);
    }

    @Override
    public List<ChartData> findByDateRange(DateRange dateRange) {
        log.debug("Поиск данных за период: {} - {}", dateRange.startDate(), dateRange.endDate());
        return jpaRepository.findAllInDateRange(
                        dateRange.startDate(),
                        dateRange.endDate()
                ).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public ChartData save(ChartData data) {
        log.debug("Сохранение данных: {}", data);
        var entity = toEntity(data);
        var saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<ChartData> saveAll(List<ChartData> dataList) {
        log.debug("Пакетное сохранение {} записей", dataList.size());
        var entities = dataList.stream()
                .map(this::toEntity)
                .toList();
        var saved = jpaRepository.saveAll(entities);
        return saved.stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean deleteByDate(LocalDate date) {
        log.debug("Удаление данных за дату: {}", date);
        var deleted = jpaRepository.deleteByDate(date);
        return deleted > 0;
    }

    @Override
    public boolean existsByDate(LocalDate date) {
        log.debug("Проверка существования данных за дату: {}", date);
        return jpaRepository.existsByDate(date);
    }

    private ChartData toDomain(ChartDataEntity entity) {
        return ChartData.builder()
                .date(entity.getDate())
                .cost(Money.of(entity.getCost().doubleValue()))
                .cpa(Cpa.of(entity.getCpa().doubleValue()))
                .roi(Roi.of(entity.getRoi().doubleValue()))
                .conversions(entity.getConversions())
                .build();
    }

    private ChartDataEntity toEntity(ChartData data) {
        return ChartDataEntity.builder()
                .date(data.date())
                .cost(java.math.BigDecimal.valueOf(data.cost().getAmount().doubleValue()))
                .cpa(java.math.BigDecimal.valueOf(data.cpa().getValue()))
                .roi(java.math.BigDecimal.valueOf(data.roi().value()))
                .conversions(data.conversions())
                .build();
    }
}