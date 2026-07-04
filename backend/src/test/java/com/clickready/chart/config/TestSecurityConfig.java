package com.clickready.chart.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import lombok.extern.slf4j.Slf4j;

/**
 * Тестовая конфигурация безопасности.
 * Полностью отключает Security для E2E и интеграционных тестов.
 *
 * <p>Используется для тестов, где не требуется аутентификация.
 * Заменяет основную SecurityConfig в контексте тестов.
 *
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
@Slf4j
@TestConfiguration
public class TestSecurityConfig {

    /**
     * Создает SecurityFilterChain для тестов.
     * Отключает CSRF и разрешает все запросы без аутентификации.
     *
     * @param http конфигурация HTTP безопасности
     * @return SecurityFilterChain для тестов
     * @throws Exception если произошла ошибка конфигурации
     */
    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        log.info("🔓 Загрузка тестовой Security конфигурации (без аутентификации)");

        http
                // Отключаем CSRF для REST API
                .csrf(AbstractHttpConfigurer::disable)

                // Статистика сессий — stateless
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Разрешаем все запросы без аутентификации
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        log.info("✅ Тестовая Security конфигурация загружена");
        return http.build();
    }
}