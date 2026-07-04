package com.clickready.chart.infrastructure.config;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

/**
 * Конфигурация RestTemplate для HTTP запросов.
 *
 * <p>Настраивает:
 * <ul>
 *   <li>Таймауты подключения и чтения</li>
 *   <li>Логирование запросов</li>
 *   <li>Обработку ошибок</li>
 * </ul>
 *
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
@Slf4j
@Configuration
public class RestTemplateConfig {

    private static final int CONNECT_TIMEOUT = 10000;  // 10 секунд
    private static final int READ_TIMEOUT = 30000;     // 30 секунд
    private static final int MAX_TOTAL_CONNECTIONS = 100;
    private static final int MAX_PER_ROUTE = 20;

    /**
     * Создает настроенный RestTemplate.
     */
    @Bean
    public RestTemplate restTemplate() {
        log.info("Инициализация RestTemplate");

        var builder = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(CONNECT_TIMEOUT))
                .setReadTimeout(Duration.ofMillis(READ_TIMEOUT))
                .additionalMessageConverters(new MappingJackson2HttpMessageConverter())
                .requestFactory(() -> {
                    var factory = new SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout(CONNECT_TIMEOUT);
                    factory.setReadTimeout(READ_TIMEOUT);
                    factory.setBufferRequestBody(false);
                    return factory;
                });

        var restTemplate = builder.build();

        // Добавляем перехватчики для логирования
        restTemplate.getInterceptors().add((request, body, execution) -> {
            var startTime = System.currentTimeMillis();
            var response = execution.execute(request, body);
            var duration = System.currentTimeMillis() - startTime;

            log.debug("HTTP {} {} -> {} ({}ms)",
                    request.getMethod(),
                    request.getURI(),
                    response.getStatusCode(),
                    duration
            );

            return response;
        });

        log.info("RestTemplate инициализирован с таймаутами: connect={}ms, read={}ms",
                CONNECT_TIMEOUT, READ_TIMEOUT);

        return restTemplate;
    }

    /**
     * RestTemplate с буферизацией для логирования тела запроса.
     */
    @Bean
    public RestTemplate loggingRestTemplate() {
        log.info("Инициализация Logging RestTemplate");

        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(READ_TIMEOUT);

        var bufferingFactory = new BufferingClientHttpRequestFactory(factory);

        var restTemplate = new RestTemplate(bufferingFactory);

        // Добавляем перехватчик для логирования тела запроса/ответа
        restTemplate.getInterceptors().add((request, body, execution) -> {
            var startTime = System.currentTimeMillis();
            var response = execution.execute(request, body);
            var duration = System.currentTimeMillis() - startTime;

            if (log.isDebugEnabled()) {
                log.debug("HTTP {} {} -> {} ({}ms)",
                        request.getMethod(),
                        request.getURI(),
                        response.getStatusCode(),
                        duration
                );
            }

            return response;
        });

        return restTemplate;
    }
}