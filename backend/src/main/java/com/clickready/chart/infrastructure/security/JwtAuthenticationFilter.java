package com.clickready.chart.infrastructure.security;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT аутентификационный фильтр.
 *
 * <p>Перехватывает каждый HTTP запрос и проверяет наличие JWT токена
 * в заголовке Authorization. Если токен валиден, устанавливает
 * аутентификацию в SecurityContext.
 *
 * <p>Особенности:
 * <ul>
 *   <li>Пропускает публичные эндпоинты (без токена)</li>
 *   <li>Извлекает токен из заголовка Authorization: Bearer</li>
 *   <li>Валидирует токен и устанавливает аутентификацию</li>
 *   <li>Не выбрасывает исключения — просто пропускает запрос без аутентификации</li>
 * </ul>
 *
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Префикс Bearer в заголовке Authorization.
     */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Длина префикса Bearer (для быстрого извлечения токена).
     */
    private static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Основной метод фильтрации запросов.
     *
     * @param request     HTTP запрос
     * @param response    HTTP ответ
     * @param filterChain цепочка фильтров
     * @throws ServletException если произошла ошибка сервлета
     * @throws IOException      если произошла ошибка ввода-вывода
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        var requestURI = request.getRequestURI();

        try {
            // Пропускаем публичные эндпоинты (без аутентификации)
            if (isPublicEndpoint(requestURI)) {
                log.debug("Публичный эндпоинт: {}, пропускаем аутентификацию", requestURI);
                filterChain.doFilter(request, response);
                return;
            }

            // Извлекаем токен из заголовка
            var token = extractToken(request);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                // Получаем аутентификацию из токена
                var authentication = jwtTokenProvider.getAuthentication(token);

                // Устанавливаем детали запроса
                if (authentication instanceof UsernamePasswordAuthenticationToken) {
                    var details = new WebAuthenticationDetailsSource().buildDetails(request);
                    ((UsernamePasswordAuthenticationToken) authentication).setDetails(details);
                }

                // Устанавливаем аутентификацию в SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Установлена аутентификация для пользователя: {} на эндпоинте: {}",
                        authentication.getName(), requestURI);
            } else {
                // Токен отсутствует или невалиден — просто пропускаем без аутентификации
                // Security вернёт 401 через authenticationEntryPoint
                log.debug("Токен отсутствует или невалиден для эндпоинта: {}", requestURI);
            }

        } catch (Exception e) {
            // Логируем ошибку, но не прерываем цепочку фильтров
            // Security обработает ошибку через authenticationEntryPoint
            log.error("Ошибка при аутентификации на эндпоинте {}: {}", requestURI, e.getMessage());
        }

        // Продолжаем цепочку фильтров
        filterChain.doFilter(request, response);
    }

    /**
     * Проверяет, является ли эндпоинт публичным.
     *
     * @param uri URI запроса
     * @return true если эндпоинт публичный (не требует аутентификации)
     */
    private boolean isPublicEndpoint(String uri) {
        return uri.startsWith("/swagger-ui") ||
               uri.startsWith("/v3/api-docs") ||
               uri.startsWith("/api-docs") ||
               uri.startsWith("/actuator/health") ||
               uri.startsWith("/actuator/info") ||
               uri.equals("/api/v1/chart/health");
    }

    /**
     * Извлекает JWT токен из заголовка Authorization.
     *
     * @param request HTTP запрос
     * @return JWT токен или null если токен отсутствует
     */
    private String extractToken(HttpServletRequest request) {
        var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || authHeader.isEmpty()) {
            log.trace("Заголовок Authorization отсутствует");
            return null;
        }

        if (!authHeader.startsWith(BEARER_PREFIX)) {
            log.trace("Заголовок Authorization не содержит Bearer префикс: {}", authHeader);
            return null;
        }

        var token = authHeader.substring(BEARER_PREFIX_LENGTH);

        if (token.isEmpty()) {
            log.trace("Пустой токен в заголовке Authorization");
            return null;
        }

        log.trace("Токен извлечен: {}", maskToken(token));
        return token;
    }

    /**
     * Маскирует токен для логирования.
     * Показывает первые 10 символов, остальное заменяет звёздочками.
     *
     * @param token JWT токен
     * @return замаскированный токен
     */
    private String maskToken(String token) {
        if (token == null || token.length() <= 10) {
            return "***";
        }
        return token.substring(0, 10) + "...";
    }
}