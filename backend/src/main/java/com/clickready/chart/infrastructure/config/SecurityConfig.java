package com.clickready.chart.infrastructure.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.clickready.chart.infrastructure.security.JwtAuthenticationFilter;
import com.clickready.chart.infrastructure.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Конфигурация безопасности приложения.
 *
 * <p>Настраивает:
 * <ul>
 *   <li>JWT аутентификацию через фильтр</li>
 *   <li>CORS политику для фронтенда</li>
 *   <li>Публичные эндпоинты (без аутентификации)</li>
 *   <li>Обработку ошибок аутентификации (401/403)</li>
 *   <li>Stateless сессии (без сохранения состояния)</li>
 * </ul>
 *
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Публичные эндпоинты, доступные без аутентификации.
     * <p>
     * ✅ ДОБАВЛЕНЫ: /api/v1/auth/** и /api/v1/test/** для получения токена
     */
    private static final String[] PUBLIC_ENDPOINTS = {
            // Auth endpoints (для получения токена)
            "/api/v1/auth/**",
            "/api/v1/test/**",
            // Health
            "/api/v1/chart/health",
            "/actuator/health",
            "/actuator/info",
            // Swagger
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/api-docs/**"
    };

    /**
     * Настройка Security Filter Chain.
     *
     * @param http конфигурация HTTP безопасности
     * @return SecurityFilterChain для Spring Security
     * @throws Exception если произошла ошибка конфигурации
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Настройка Security Filter Chain");
        log.info("Публичные эндпоинты: {}", Arrays.toString(PUBLIC_ENDPOINTS));

        http
                // Отключаем CSRF для REST API (используем JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Настройка CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Статистика сессий (stateless — не храним сессии)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ОБРАБОТЧИКИ ОШИБОК АУТЕНТИФИКАЦИИ
                .exceptionHandling(exception -> exception
                        // 401 Unauthorized — нет токена или невалидный
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("Аутентификация не пройдена: {}", authException.getMessage());
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("""
                                    {
                                        "error": "Необходима аутентификация",
                                        "status": 401,
                                        "message": "Для доступа к этому ресурсу требуется JWT токен"
                                    }
                                    """);
                        })
                        // 403 Forbidden — токен есть, но недостаточно прав
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.warn("Доступ запрещен: {}", accessDeniedException.getMessage());
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("""
                                    {
                                        "error": "Доступ запрещен",
                                        "status": 403,
                                        "message": "У вас недостаточно прав для выполнения этой операции"
                                    }
                                    """);
                        })
                )

                // Настройка авторизации
                .authorizeHttpRequests(auth -> auth
                        // ✅ Публичные эндпоинты (без токена)
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        // Все остальные требуют аутентификацию
                        .anyRequest().authenticated()
                )

                // Добавляем JWT фильтр перед стандартным UsernamePasswordAuthenticationFilter
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        log.info("Security Filter Chain настроена успешно");
        return http.build();
    }

    /**
     * Настройка CORS политики.
     *
     * <p>Разрешает запросы с указанных доменов и методы.
     *
     * @return CorsConfigurationSource с настройками
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Настройка CORS политики");

        var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:3001",
                "https://clickready.com"
        ));
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(List.of(
                "Authorization"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("CORS политика настроена: origins={}, methods={}",
                configuration.getAllowedOrigins(),
                configuration.getAllowedMethods());

        return source;
    }

    /**
     * PasswordEncoder для хеширования паролей.
     * Используется BCrypt — стандарт для Spring Security.
     *
     * @return BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("Инициализация PasswordEncoder (BCrypt)");
        return new BCryptPasswordEncoder();
    }
}