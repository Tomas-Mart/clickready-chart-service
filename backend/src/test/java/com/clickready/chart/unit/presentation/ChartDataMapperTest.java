package com.clickready.chart.unit.presentation;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.clickready.chart.domain.model.ChartData;
import com.clickready.chart.domain.valueobject.Cpa;
import com.clickready.chart.domain.valueobject.Money;
import com.clickready.chart.domain.valueobject.Roi;
import com.clickready.chart.presentation.dto.ChartDataRequest;
import com.clickready.chart.presentation.mapper.ChartDataMapper;
import com.clickready.chart.presentation.mapper.ChartDataMapperImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit тесты для маппера ChartDataMapper.
 *
 * <p>Тестирует преобразования между слоями:
 * <ul>
 *   <li>DTO -> Domain (Request to Domain)</li>
 *   <li>Domain -> DTO (Domain to Response)</li>
 *   <li>Entity -> Domain (Entity to Domain)</li>
 *   <li>Domain -> Entity (Domain to Entity)</li>
 * </ul>
 *
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
@DisplayName("Тесты маппера ChartDataMapper")
class ChartDataMapperTest {

    private ChartDataMapper mapper;

    private static final LocalDate TEST_DATE = LocalDate.of(2026, 6, 13);
    private static final double TEST_COST = 55.65;
    private static final double TEST_CPA = 0.79;
    private static final double TEST_ROI = 56.33;
    private static final int TEST_CONVERSIONS = 70;

    @BeforeEach
    void setUp() {
        // ✅ Используем ручную реализацию маппера
        mapper = new ChartDataMapperImpl();
    }

    // ============================================================
    // DTO -> DOMAIN TESTS
    // ============================================================

    @Test
    @DisplayName("Маппинг Request -> Domain")
    void shouldMapRequestToDomain() {
        // Arrange
        var request = new ChartDataRequest(
                TEST_DATE,
                TEST_COST,
                TEST_CPA,
                TEST_ROI,
                TEST_CONVERSIONS
        );

        // Act
        var domain = mapper.toDomain(request);

        // Assert
        assertThat(domain.date()).isEqualTo(TEST_DATE);
        assertThat(domain.cost().getValue()).isEqualTo(TEST_COST);
        assertThat(domain.cpa().getValue()).isEqualTo(TEST_CPA);
        assertThat(domain.roi().getValue()).isEqualTo(TEST_ROI);
        assertThat(domain.conversions()).isEqualTo(TEST_CONVERSIONS);
        assertThat(domain.isValid()).isTrue();
    }

    @Test
    @DisplayName("Маппинг списка Request -> Domain")
    void shouldMapRequestListToDomainList() {
        // Arrange
        var request = new ChartDataRequest(
                TEST_DATE,
                TEST_COST,
                TEST_CPA,
                TEST_ROI,
                TEST_CONVERSIONS
        );
        var requestList = List.of(request);

        // Act
        var domainList = mapper.toDomainListFromRequests(requestList);

        // Assert
        assertThat(domainList).hasSize(1);
        assertThat(domainList.get(0).date()).isEqualTo(TEST_DATE);
        assertThat(domainList.get(0).cost().getValue()).isEqualTo(TEST_COST);
    }

    // ============================================================
    // DOMAIN -> DTO TESTS
    // ============================================================

    @Test
    @DisplayName("Маппинг Domain -> Response")
    void shouldMapDomainToResponse() {
        // Arrange
        var domain = new ChartData(
                TEST_DATE,
                Money.of(TEST_COST),
                Cpa.of(TEST_CPA),
                Roi.of(TEST_ROI),
                TEST_CONVERSIONS
        );

        // Act
        var response = mapper.toResponse(domain);

        // Assert
        assertThat(response.date()).isEqualTo(TEST_DATE);
        assertThat(response.cost()).isEqualTo(TEST_COST);
        assertThat(response.cpa()).isEqualTo(TEST_CPA);
        assertThat(response.roi()).isEqualTo(TEST_ROI);
        assertThat(response.conversions()).isEqualTo(TEST_CONVERSIONS);
        assertThat(response.profitable()).isTrue();
    }

    @Test
    @DisplayName("Маппинг списка Domain -> Response")
    void shouldMapDomainListToResponseList() {
        // Arrange
        var domainList = List.of(
                new ChartData(
                        TEST_DATE,
                        Money.of(TEST_COST),
                        Cpa.of(TEST_CPA),
                        Roi.of(TEST_ROI),
                        TEST_CONVERSIONS
                )
        );

        // Act
        var responseList = mapper.toResponseList(domainList);

        // Assert
        assertThat(responseList).hasSize(1);
        assertThat(responseList.get(0).date()).isEqualTo(TEST_DATE);
        assertThat(responseList.get(0).cost()).isEqualTo(TEST_COST);
        assertThat(responseList.get(0).profitable()).isTrue();
    }

    // ============================================================
    // DOMAIN -> ENTITY TESTS
    // ============================================================

    @Test
    @DisplayName("Маппинг Domain -> Entity")
    void shouldMapDomainToEntity() {
        // Arrange
        var domain = new ChartData(
                TEST_DATE,
                Money.of(TEST_COST),
                Cpa.of(TEST_CPA),
                Roi.of(TEST_ROI),
                TEST_CONVERSIONS
        );

        // Act
        var entity = mapper.toEntity(domain);

        // Assert
        assertThat(entity.getDate()).isEqualTo(TEST_DATE);
        assertThat(entity.getCost().doubleValue()).isEqualTo(TEST_COST);
        assertThat(entity.getCpa().doubleValue()).isEqualTo(TEST_CPA);
        assertThat(entity.getRoi().doubleValue()).isEqualTo(TEST_ROI);
        assertThat(entity.getConversions()).isEqualTo(TEST_CONVERSIONS);
    }

    @Test
    @DisplayName("Маппинг списка Domain -> Entity")
    void shouldMapDomainListToEntityList() {
        // Arrange
        var domainList = List.of(
                new ChartData(
                        TEST_DATE,
                        Money.of(TEST_COST),
                        Cpa.of(TEST_CPA),
                        Roi.of(TEST_ROI),
                        TEST_CONVERSIONS
                )
        );

        // Act
        var entityList = mapper.toEntityList(domainList);

        // Assert
        assertThat(entityList).hasSize(1);
        assertThat(entityList.get(0).getDate()).isEqualTo(TEST_DATE);
        assertThat(entityList.get(0).getCost().doubleValue()).isEqualTo(TEST_COST);
    }

    // ============================================================
    // ENTITY -> DOMAIN TESTS
    // ============================================================

    @Test
    @DisplayName("Маппинг Entity -> Domain")
    void shouldMapEntityToDomain() {
        // Arrange
        var entity = new com.clickready.chart.infrastructure.repository.entity.ChartDataEntity();
        entity.setDate(TEST_DATE);
        entity.setCost(java.math.BigDecimal.valueOf(TEST_COST));
        entity.setCpa(java.math.BigDecimal.valueOf(TEST_CPA));
        entity.setRoi(java.math.BigDecimal.valueOf(TEST_ROI));
        entity.setConversions(TEST_CONVERSIONS);

        // Act
        var domain = mapper.toDomain(entity);

        // Assert
        assertThat(domain.date()).isEqualTo(TEST_DATE);
        assertThat(domain.cost().getValue()).isEqualTo(TEST_COST);
        assertThat(domain.cpa().getValue()).isEqualTo(TEST_CPA);
        assertThat(domain.roi().getValue()).isEqualTo(TEST_ROI);
        assertThat(domain.conversions()).isEqualTo(TEST_CONVERSIONS);
    }

    @Test
    @DisplayName("Маппинг списка Entity -> Domain")
    void shouldMapEntityListToDomainList() {
        // Arrange
        var entity = new com.clickready.chart.infrastructure.repository.entity.ChartDataEntity();
        entity.setDate(TEST_DATE);
        entity.setCost(java.math.BigDecimal.valueOf(TEST_COST));
        entity.setCpa(java.math.BigDecimal.valueOf(TEST_CPA));
        entity.setRoi(java.math.BigDecimal.valueOf(TEST_ROI));
        entity.setConversions(TEST_CONVERSIONS);
        var entityList = List.of(entity);

        // Act
        var domainList = mapper.toDomainListFromEntities(entityList);

        // Assert
        assertThat(domainList).hasSize(1);
        assertThat(domainList.get(0).date()).isEqualTo(TEST_DATE);
        assertThat(domainList.get(0).cost().getValue()).isEqualTo(TEST_COST);
    }

    // ============================================================
    // NULL SAFETY TESTS
    // ============================================================

    @Test
    @DisplayName("Маппинг с null значениями")
    void shouldHandleNullValues() {
        // Arrange
        var domain = new ChartData(
                TEST_DATE,
                Money.ZERO,
                Cpa.of(0),
                Roi.of(0),
                0
        );

        // Act
        var response = mapper.toResponse(domain);

        // Assert
        assertThat(response.cost()).isZero();
        assertThat(response.cpa()).isZero();
        assertThat(response.roi()).isZero();
        assertThat(response.profitable()).isFalse();
    }

    @Test
    @DisplayName("Маппинг с отрицательным ROI")
    void shouldHandleNegativeRoi() {
        // Arrange
        var domain = new ChartData(
                TEST_DATE,
                Money.of(100),
                Cpa.of(1),
                Roi.of(-50),
                100
        );

        // Act
        var response = mapper.toResponse(domain);

        // Assert
        assertThat(response.roi()).isEqualTo(-50);
        assertThat(response.profitable()).isFalse();
    }

    @Test
    @DisplayName("Маппинг с высоким ROI (очень прибыльный)")
    void shouldHandleHighRoi() {
        // Arrange
        var domain = new ChartData(
                TEST_DATE,
                Money.of(100),
                Cpa.of(1),
                Roi.of(250),
                100
        );

        // Act
        var response = mapper.toResponse(domain);

        // Assert
        assertThat(response.roi()).isEqualTo(250);
        assertThat(response.profitable()).isTrue();
    }

    // ============================================================
    // EXCEPTION TESTS
    // ============================================================

    @Test
    @DisplayName("Маппинг с null Request выбрасывает исключение")
    void shouldThrowExceptionWhenRequestIsNull() {
        assertThatThrownBy(() -> mapper.toDomain((ChartDataRequest) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ChartDataRequest не может быть null");
    }

    @Test
    @DisplayName("Маппинг с null Entity выбрасывает исключение")
    void shouldThrowExceptionWhenEntityIsNull() {
        assertThatThrownBy(() -> mapper.toDomain((com.clickready.chart.infrastructure.repository.entity.ChartDataEntity) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ChartDataEntity не может быть null");
    }

    @Test
    @DisplayName("Маппинг с null Domain выбрасывает исключение")
    void shouldThrowExceptionWhenDomainIsNull() {
        assertThatThrownBy(() -> mapper.toResponse(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ChartData не может быть null");
    }

    // ============================================================
    // UPDATE ENTITY TESTS
    // ============================================================

    @Test
    @DisplayName("Обновление Entity из Domain")
    void shouldUpdateEntityFromDomain() {
        // Arrange
        var mapperImpl = (ChartDataMapperImpl) mapper;

        var existingEntity = new com.clickready.chart.infrastructure.repository.entity.ChartDataEntity();
        existingEntity.setId(1L);
        existingEntity.setDate(LocalDate.of(2026, 6, 12));
        existingEntity.setCost(java.math.BigDecimal.valueOf(10));
        existingEntity.setCpa(java.math.BigDecimal.valueOf(0.5));
        existingEntity.setRoi(java.math.BigDecimal.valueOf(10));
        existingEntity.setConversions(5);
        existingEntity.setVersion(0);

        var updatedDomain = new ChartData(
                TEST_DATE,
                Money.of(TEST_COST),
                Cpa.of(TEST_CPA),
                Roi.of(TEST_ROI),
                TEST_CONVERSIONS
        );

        // Act
        var updatedEntity = mapperImpl.updateEntity(updatedDomain, existingEntity);

        // Assert
        assertThat(updatedEntity.getId()).isEqualTo(1L);
        assertThat(updatedEntity.getDate()).isEqualTo(TEST_DATE);
        assertThat(updatedEntity.getCost().doubleValue()).isEqualTo(TEST_COST);
        assertThat(updatedEntity.getCpa().doubleValue()).isEqualTo(TEST_CPA);
        assertThat(updatedEntity.getRoi().doubleValue()).isEqualTo(TEST_ROI);
        assertThat(updatedEntity.getConversions()).isEqualTo(TEST_CONVERSIONS);
        assertThat(updatedEntity.getVersion()).isZero(); // version не меняется
    }
}