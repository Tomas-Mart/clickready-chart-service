package com.clickready.chart.infrastructure.client;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.clickready.chart.domain.model.ChartData;
import com.clickready.chart.domain.valueobject.Cpa;
import com.clickready.chart.domain.valueobject.Money;
import com.clickready.chart.domain.valueobject.Roi;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Клиент для интеграции с внешними API.
 *
 * <p>Предоставляет методы для:
 * <ul>
 *   <li>Получения данных из внешних источников (Facebook Ads, Google Analytics, etc.)</li>
 *   <li>Отправки данных во внешние системы</li>
 *   <li>Обработки ошибок с Circuit Breaker и Retry</li>
 * </ul>
 *
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Базовый URL внешнего API.
     */
    @Value("${external.api.base-url:https://api.example.com}")
    private String baseUrl;

    /**
     * API ключ для аутентификации.
     */
    @Value("${external.api.api-key:}")
    private String apiKey;

    /**
     * Таймаут запроса в миллисекундах.
     */
    @Value("${external.api.timeout:30000}")
    private int timeout;

    /**
     * Получить данные из внешнего API по дате.
     *
     * @param date дата для получения данных
     * @return данные графика, или null если данные не найдены
     */
    @CircuitBreaker(name = "externalApi", fallbackMethod = "getChartDataFallback")
    @Retry(name = "externalApi", fallbackMethod = "getChartDataFallback")
    public Optional<ChartData> fetchChartDataByDate(LocalDate date) {
        log.info("Запрос данных из внешнего API за дату: {}", date);

        try {
            var url = UriComponentsBuilder
                    .fromHttpUrl(baseUrl)
                    .path("/api/v1/chart/data")
                    .queryParam("date", date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .build()
                    .toUriString();

            var headers = createHeaders();
            var entity = new HttpEntity<>(headers);

            log.debug("Выполняется GET запрос к: {}", url);
            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );

            if (response.getBody() != null && response.getBody().has("data")) {
                var dataNode = response.getBody().get("data");
                var chartData = parseChartData(dataNode);
                log.info("Данные успешно получены из внешнего API: {}", chartData);
                return Optional.of(chartData);
            }

            log.warn("Данные за {} не найдены во внешнем API", date);
            return Optional.empty();

        } catch (RestClientException e) {
            log.error("Ошибка при получении данных из внешнего API: {}", e.getMessage());
            throw new RuntimeException("Не удалось получить данные из внешнего API", e);
        }
    }

    /**
     * Получить данные из внешнего API за период.
     *
     * @param startDate начальная дата
     * @param endDate   конечная дата
     * @return список данных графика
     */
    @CircuitBreaker(name = "externalApi", fallbackMethod = "getChartDataRangeFallback")
    @Retry(name = "externalApi", fallbackMethod = "getChartDataRangeFallback")
    public List<ChartData> fetchChartDataInRange(LocalDate startDate, LocalDate endDate) {
        log.info("Запрос данных из внешнего API за период с {} по {}", startDate, endDate);

        try {
            var url = UriComponentsBuilder
                    .fromHttpUrl(baseUrl)
                    .path("/api/v1/chart/data/range")
                    .queryParam("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .queryParam("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .build()
                    .toUriString();

            var headers = createHeaders();
            var entity = new HttpEntity<>(headers);

            log.debug("Выполняется GET запрос к: {}", url);
            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );

            var result = new ArrayList<ChartData>();
            if (response.getBody() != null && response.getBody().has("data")) {
                var dataArray = response.getBody().get("data");
                if (dataArray.isArray()) {
                    for (var node : dataArray) {
                        result.add(parseChartData(node));
                    }
                }
            }

            log.info("Получено {} записей из внешнего API", result.size());
            return result;

        } catch (RestClientException e) {
            log.error("Ошибка при получении данных из внешнего API: {}", e.getMessage());
            throw new RuntimeException("Не удалось получить данные из внешнего API", e);
        }
    }

    /**
     * Отправить данные во внешний API.
     *
     * @param chartData данные для отправки
     * @return true если отправка успешна
     */
    @CircuitBreaker(name = "externalApi", fallbackMethod = "sendChartDataFallback")
    @Retry(name = "externalApi", fallbackMethod = "sendChartDataFallback")
    public boolean sendChartData(ChartData chartData) {
        log.info("Отправка данных во внешний API: {}", chartData);

        try {
            var url = UriComponentsBuilder
                    .fromHttpUrl(baseUrl)
                    .path("/api/v1/chart/data")
                    .build()
                    .toUriString();

            var headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody;
            try {
                requestBody = objectMapper.writeValueAsString(chartData);
            } catch (JsonProcessingException e) {
                log.error("Ошибка сериализации данных для отправки: {}", e.getMessage());
                throw new RuntimeException("Не удалось сериализовать данные для отправки", e);
            }

            var entity = new HttpEntity<>(requestBody, headers);

            log.debug("Выполняется POST запрос к: {}", url);
            var response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    JsonNode.class
            );

            var success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                log.info("Данные успешно отправлены во внешний API");
            } else {
                log.warn("Не удалось отправить данные во внешний API: {}", response.getStatusCode());
            }
            return success;

        } catch (RestClientException e) {
            log.error("Ошибка при отправке данных во внешний API: {}", e.getMessage());
            throw new RuntimeException("Не удалось отправить данные во внешний API", e);
        }
    }

    /**
     * Проверить доступность внешнего API.
     *
     * @return true если API доступен
     */
    @CircuitBreaker(name = "externalApi", fallbackMethod = "healthCheckFallback")
    public boolean healthCheck() {
        log.debug("Проверка доступности внешнего API");

        try {
            var url = UriComponentsBuilder
                    .fromHttpUrl(baseUrl)
                    .path("/actuator/health")
                    .build()
                    .toUriString();

            var headers = createHeaders();
            var entity = new HttpEntity<>(headers);

            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );

            var isHealthy = response.getStatusCode().is2xxSuccessful();
            log.debug("Внешний API доступен: {}", isHealthy);
            return isHealthy;

        } catch (RestClientException e) {
            log.warn("Внешний API недоступен: {}", e.getMessage());
            return false;
        }
    }

    // ============================================================
    // FALLBACK МЕТОДЫ
    // ============================================================

    /**
     * Fallback метод для fetchChartDataByDate.
     */
    public Optional<ChartData> getChartDataFallback(LocalDate date, Throwable throwable) {
        log.warn("Fallback для запроса данных за {}: {}", date, throwable.getMessage());
        return Optional.empty();
    }

    /**
     * Fallback метод для fetchChartDataInRange.
     */
    public List<ChartData> getChartDataRangeFallback(LocalDate startDate, LocalDate endDate, Throwable throwable) {
        log.warn("Fallback для запроса данных за период: {}", throwable.getMessage());
        return List.of();
    }

    /**
     * Fallback метод для sendChartData.
     */
    public boolean sendChartDataFallback(ChartData chartData, Throwable throwable) {
        log.warn("Fallback для отправки данных: {}", throwable.getMessage());
        return false;
    }

    /**
     * Fallback метод для healthCheck.
     */
    public boolean healthCheckFallback(Throwable throwable) {
        log.warn("Fallback для healthCheck: {}", throwable.getMessage());
        return false;
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ============================================================

    /**
     * Создать HTTP заголовки для запроса.
     *
     * @return HttpHeaders
     */
    private HttpHeaders createHeaders() {
        var headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        if (apiKey != null && !apiKey.isEmpty()) {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
        }

        return headers;
    }

    /**
     * Распарсить JSON в ChartData.
     *
     * @param node JSON узел с данными
     * @return ChartData объект
     */
    private ChartData parseChartData(JsonNode node) {
        var date = LocalDate.parse(node.get("date").asText());
        var cost = Money.of(node.get("cost").asDouble());
        var cpa = Cpa.of(node.get("cpa").asDouble());
        var roi = Roi.of(node.get("roi").asDouble());
        var conversions = node.get("conversions").asInt();

        return ChartData.builder()
                .date(date)
                .cost(cost)
                .cpa(cpa)
                .roi(roi)
                .conversions(conversions)
                .build();
    }
}