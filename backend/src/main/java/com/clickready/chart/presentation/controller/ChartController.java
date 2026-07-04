package com.clickready.chart.presentation.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.clickready.chart.application.port.in.GetChartDataUseCase;
import com.clickready.chart.application.port.in.SaveChartDataUseCase;
import com.clickready.chart.application.port.in.UpdateChartDataUseCase;
import com.clickready.chart.presentation.dto.ChartDataRequest;
import com.clickready.chart.presentation.dto.ChartDataResponse;
import com.clickready.chart.presentation.mapper.ChartDataMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/chart")
@RequiredArgsConstructor
@Tag(name = "Chart API", description = "API для работы с данными графика ClickReady")
public class ChartController {

    private final GetChartDataUseCase getChartDataUseCase;
    private final SaveChartDataUseCase saveChartDataUseCase;
    private final UpdateChartDataUseCase updateChartDataUseCase;
    private final ChartDataMapper mapper;

    // ============================================================
    // GET ENDPOINTS
    // ============================================================

    @Operation(summary = "Получить все данные для графика")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные успешно получены"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/data")
    public ResponseEntity<List<ChartDataResponse>> getChartData() {
        log.debug("REST запрос: получение всех данных графика");
        var data = getChartDataUseCase.getChartData();
        // ✅ Теперь метод принимает List<?>
        var response = mapper.toResponseList(data);
        log.debug("Найдено {} записей", response.size());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получить данные за период")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные успешно получены"),
            @ApiResponse(responseCode = "400", description = "Некорректный период")
    })
    @GetMapping("/data/range")
    public ResponseEntity<List<ChartDataResponse>> getChartDataInRange(
            @Parameter(description = "Начальная дата (yyyy-MM-dd)", example = "2026-06-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Конечная дата (yyyy-MM-dd)", example = "2026-06-30")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.debug("REST запрос: получение данных за период {} - {}", startDate, endDate);
        var data = getChartDataUseCase.getChartDataInRange(startDate, endDate);
        // ✅ Теперь метод принимает List<?>
        var response = mapper.toResponseList(data);
        log.debug("Найдено {} записей за период", response.size());
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // POST ENDPOINTS
    // ============================================================

    @Operation(summary = "Сохранить новые данные графика")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Данные успешно сохранены"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные"),
            @ApiResponse(responseCode = "409", description = "Конфликт данных (дата уже существует)")
    })
    @PostMapping("/data")
    public ResponseEntity<ChartDataResponse> saveChartData(
            @Valid @RequestBody ChartDataRequest request
    ) {
        log.info("REST запрос: сохранение данных для даты {}", request.date());

        var domainData = mapper.toDomain(request);
        var savedData = saveChartDataUseCase.saveChartData(domainData);
        var response = mapper.toResponse(savedData);

        log.info("✅ Данные сохранены для даты {}", response.date());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Пакетное сохранение данных графика")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Данные успешно сохранены"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные")
    })
    @PostMapping("/data/batch")
    public ResponseEntity<List<ChartDataResponse>> saveAllChartData(
            @Valid @RequestBody List<ChartDataRequest> requests
    ) {
        log.info("REST запрос: пакетное сохранение {} записей", requests.size());

        var domainData = mapper.toDomainListFromRequests(requests);
        var savedData = saveChartDataUseCase.saveAllChartData(domainData);
        // ✅ Теперь метод принимает List<?>
        var response = mapper.toResponseList(savedData);

        log.info("✅ Пакетно сохранено {} записей", response.size());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ============================================================
    // DELETE ENDPOINTS
    // ============================================================

    @Operation(summary = "Удалить данные за дату")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Данные удалены"),
            @ApiResponse(responseCode = "404", description = "Данные не найдены")
    })
    @DeleteMapping("/data/{date}")
    public ResponseEntity<Void> deleteChartData(
            @Parameter(description = "Дата (yyyy-MM-dd)", example = "2026-06-13")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("REST запрос: удаление данных за {}", date);
        updateChartDataUseCase.deleteChartData(date);
        log.info("✅ Данные удалены за {}", date);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // HEALTH ENDPOINT
    // ============================================================

    @Operation(summary = "Health check", description = "Проверка доступности сервиса")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Сервис работает")
    })
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.debug("Health check запрос");
        return ResponseEntity.ok("Service is healthy");
    }
}