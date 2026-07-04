package com.clickready.chart.integration;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.clickready.chart.application.port.in.GetChartDataUseCase;
import com.clickready.chart.application.port.in.SaveChartDataUseCase;
import com.clickready.chart.application.port.in.UpdateChartDataUseCase;
import com.clickready.chart.config.TestCorsConfig;
import com.clickready.chart.domain.model.ChartData;
import com.clickready.chart.domain.valueobject.Cpa;
import com.clickready.chart.domain.valueobject.Money;
import com.clickready.chart.domain.valueobject.Roi;
import com.clickready.chart.infrastructure.security.JwtTokenProvider;
import com.clickready.chart.presentation.dto.ChartDataRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Import({TestCorsConfig.class})
@DisplayName("Интеграционные тесты контроллера")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChartControllerTest {

    // ============================================================
    // ПОЛЯ
    // ============================================================

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;  // ✅ Добавить

    @MockBean
    private GetChartDataUseCase getChartDataUseCase;

    @MockBean
    private SaveChartDataUseCase saveChartDataUseCase;

    @MockBean
    private UpdateChartDataUseCase updateChartDataUseCase;

    private static final LocalDate TEST_DATE = LocalDate.of(2026, 6, 13);
    private static final double TEST_COST = 55.65;
    private static final double TEST_CPA = 0.79;
    private static final double TEST_ROI = 56.33;
    private static final int TEST_CONVERSIONS = 70;

    private String validToken;  // ✅ Добавить

    @BeforeEach
    void setUp() {
        validToken = jwtTokenProvider.generateToken("testuser", List.of("ROLE_USER"));
    }

    // ============================================================
    // GET TESTS
    // ============================================================

    @Test
    @DisplayName("GET /api/v1/chart/data - успешный запрос")
    void shouldGetChartData() throws Exception {
        var mockData = List.of(
                new ChartData(
                        TEST_DATE,
                        Money.of(TEST_COST),
                        Cpa.of(TEST_CPA),
                        Roi.of(TEST_ROI),
                        TEST_CONVERSIONS
                )
        );
        when(getChartDataUseCase.getChartData()).thenReturn(mockData);

        mockMvc.perform(get("/api/v1/chart/data")
                        .header("Authorization", "Bearer " + validToken))  // ✅ Добавить токен
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-06-13"))
                .andExpect(jsonPath("$[0].cost").value(55.65))
                .andExpect(jsonPath("$[0].cpa").value(0.79))
                .andExpect(jsonPath("$[0].roi").value(56.33))
                .andExpect(jsonPath("$[0].conversions").value(70))
                .andExpect(jsonPath("$[0].profitable").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/chart/data/range - успешный запрос")
    void shouldGetChartDataInRange() throws Exception {
        var mockData = List.of(
                new ChartData(
                        TEST_DATE,
                        Money.of(TEST_COST),
                        Cpa.of(TEST_CPA),
                        Roi.of(TEST_ROI),
                        TEST_CONVERSIONS
                )
        );
        when(getChartDataUseCase.getChartDataInRange(any(), any())).thenReturn(mockData);

        mockMvc.perform(get("/api/v1/chart/data/range")
                        .header("Authorization", "Bearer " + validToken)  // ✅ Добавить токен
                        .param("startDate", "2026-06-01")
                        .param("endDate", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-06-13"))
                .andExpect(jsonPath("$[0].cost").value(55.65));
    }

    @Test
    @DisplayName("GET /api/v1/chart/data/range - пустой результат")
    void shouldReturnEmptyListWhenNoDataInRange() throws Exception {
        when(getChartDataUseCase.getChartDataInRange(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/chart/data/range")
                        .header("Authorization", "Bearer " + validToken)  // ✅ Добавить токен
                        .param("startDate", "2026-06-01")
                        .param("endDate", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/chart/data/range - некорректные даты")
    void shouldReturnBadRequestForInvalidDateRange() throws Exception {
        mockMvc.perform(get("/api/v1/chart/data/range")
                        .header("Authorization", "Bearer " + validToken)  // ✅ Добавить токен
                        .param("startDate", "invalid")
                        .param("endDate", "2026-06-30"))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // POST TESTS
    // ============================================================

    @Test
    @DisplayName("POST /api/v1/chart/data - успешное создание")
    void shouldCreateChartData() throws Exception {
        var request = new ChartDataRequest(
                TEST_DATE,
                TEST_COST,
                TEST_CPA,
                TEST_ROI,
                TEST_CONVERSIONS
        );

        var savedData = new ChartData(
                TEST_DATE,
                Money.of(TEST_COST),
                Cpa.of(TEST_CPA),
                Roi.of(TEST_ROI),
                TEST_CONVERSIONS
        );
        when(saveChartDataUseCase.saveChartData(any())).thenReturn(savedData);

        mockMvc.perform(post("/api/v1/chart/data")
                        .header("Authorization", "Bearer " + validToken)  // ✅ Добавить токен
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.date").value("2026-06-13"))
                .andExpect(jsonPath("$.cost").value(55.65))
                .andExpect(jsonPath("$.cpa").value(0.79))
                .andExpect(jsonPath("$.roi").value(56.33))
                .andExpect(jsonPath("$.conversions").value(70))
                .andExpect(jsonPath("$.profitable").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/chart/data - невалидные данные (null дата)")
    void shouldReturnBadRequestForInvalidData() throws Exception {
        var request = new ChartDataRequest(
                null,
                TEST_COST,
                TEST_CPA,
                TEST_ROI,
                TEST_CONVERSIONS
        );

        mockMvc.perform(post("/api/v1/chart/data")
                        .header("Authorization", "Bearer " + validToken)  // ✅ Добавить токен
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/chart/data - отрицательные конверсии")
    void shouldReturnBadRequestForNegativeConversions() throws Exception {
        var request = new ChartDataRequest(
                TEST_DATE,
                TEST_COST,
                TEST_CPA,
                TEST_ROI,
                -5
        );
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/chart/data")
                        .header("Authorization", "Bearer " + validToken)  // ✅ Добавить токен
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/chart/data - отрицательная стоимость")
    void shouldReturnBadRequestForNegativeCost() throws Exception {
        var request = new ChartDataRequest(
                TEST_DATE,
                -55.65,
                TEST_CPA,
                TEST_ROI,
                TEST_CONVERSIONS
        );

        mockMvc.perform(post("/api/v1/chart/data")
                        .header("Authorization", "Bearer " + validToken)  // ✅ Добавить токен
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/chart/data/batch - пакетное создание")
    void shouldBatchCreateChartData() throws Exception {
        var requests = List.of(
                new ChartDataRequest(
                        LocalDate.of(2026, 6, 13),
                        55.65,
                        0.79,
                        56.33,
                        70
                ),
                new ChartDataRequest(
                        LocalDate.of(2026, 6, 14),
                        80.0,
                        0.2,
                        220.0,
                        110
                )
        );

        var savedData = List.of(
                new ChartData(
                        LocalDate.of(2026, 6, 13),
                        Money.of(55.65),
                        Cpa.of(0.79),
                        Roi.of(56.33),
                        70
                ),
                new ChartData(
                        LocalDate.of(2026, 6, 14),
                        Money.of(80.0),
                        Cpa.of(0.2),
                        Roi.of(220.0),
                        110
                )
        );
        when(saveChartDataUseCase.saveAllChartData(any())).thenReturn(savedData);

        mockMvc.perform(post("/api/v1/chart/data/batch")
                        .header("Authorization", "Bearer " + validToken)  // ✅ Добавить токен
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].date").value("2026-06-13"))
                .andExpect(jsonPath("$[1].date").value("2026-06-14"))
                .andExpect(jsonPath("$[0].cost").value(55.65))
                .andExpect(jsonPath("$[1].cost").value(80.0));
    }

    // ============================================================
    // DELETE TESTS
    // ============================================================

    @Test
    @DisplayName("DELETE /api/v1/chart/data/{date} - успешное удаление")
    void shouldDeleteChartData() throws Exception {
        doNothing().when(updateChartDataUseCase).deleteChartData(any());

        mockMvc.perform(delete("/api/v1/chart/data/2026-06-13")
                        .header("Authorization", "Bearer " + validToken))  // ✅ Добавить токен
                .andExpect(status().isNoContent());
    }

    // ============================================================
    // HEALTH TESTS
    // ============================================================

    @Test
    @DisplayName("GET /api/v1/chart/health - проверка здоровья")
    void shouldReturnHealthStatus() throws Exception {
        // ✅ Health check НЕ ТРЕБУЕТ токена (публичный эндпоинт)
        mockMvc.perform(get("/api/v1/chart/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Service is healthy"));
    }

    // ============================================================
    // CORS TESTS
    // ============================================================

    @Test
    @DisplayName("OPTIONS /api/v1/chart/data - CORS заголовки")
    void shouldReturnCorsHeaders() throws Exception {
        // ✅ OPTIONS НЕ ТРЕБУЕТ токена
        mockMvc.perform(options("/api/v1/chart/data")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())  // ✅ 200
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }
}