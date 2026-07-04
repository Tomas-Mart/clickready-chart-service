package com.clickready.chart.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Провайдер JWT токенов.
 *
 * <p>Отвечает за:
 * <ul>
 *   <li>Генерацию JWT токенов</li>
 *   <li>Валидацию JWT токенов</li>
 *   <li>Извлечение аутентификационной информации</li>
 *   <li>Обновление токенов</li>
 * </ul>
 *
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
@Slf4j
@Component
public class JwtTokenProvider {

    /**
     * Секретный ключ для подписи JWT.
     * Должен быть минимум 256 бит (32 символа) для HS256.
     */
    @Value("${jwt.secret:defaultSecretKeyForDevelopmentOnly12345678901234567890}")
    private String secretKey;

    /**
     * Время жизни токена в миллисекундах (по умолчанию 1 час).
     */
    @Value("${jwt.expiration:3600000}")
    private long expirationMs;

    /**
     * Время жизни refresh токена (по умолчанию 7 дней).
     */
    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpirationMs;

    /**
     * Ключ для подписи JWT.
     */
    private SecretKey key;

    /**
     * Инициализация ключа после создания бина.
     */
    @PostConstruct
    public void init() {
        log.info("Инициализация JwtTokenProvider");

        byte[] keyBytes;

        // Пробуем декодировать secretKey как Base64
        try {
            keyBytes = Base64.getDecoder().decode(secretKey);
            log.info("JWT ключ декодирован из Base64 (длина: {} байт)", keyBytes.length);
        } catch (IllegalArgumentException e) {
            // Если секрет не в Base64, используем его напрямую
            keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            log.info("JWT ключ получен из строки (длина: {} байт)", keyBytes.length);
        }

        // Проверяем, что ключ >= 256 бит (32 байта)
        if (keyBytes.length < 32) {
            log.warn("JWT секрет слишком короткий ({} байт), дополняем до 32 байт", keyBytes.length);
            var padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            keyBytes = padded;
            log.info("JWT ключ дополнен до {} байт", keyBytes.length);
        }

        key = Keys.hmacShaKeyFor(keyBytes);

        log.info("JWT токен провайдер инициализирован: expiration={}ms, refreshExpiration={}ms",
                expirationMs, refreshExpirationMs);
    }

    /**
     * Генерирует JWT токен для пользователя.
     *
     * @param username имя пользователя
     * @param roles    роли пользователя
     * @return JWT токен
     */
    public String generateToken(String username, List<String> roles) {
        log.debug("Генерация JWT токена для пользователя: {}", username);

        var now = Instant.now();
        var expiryDate = Date.from(now.plusMillis(expirationMs));

        var claims = Jwts.claims()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(expiryDate);

        // Добавляем роли в claims
        if (roles != null && !roles.isEmpty()) {
            claims.put("roles", roles);
        }

        var token = Jwts.builder()
                .setClaims(claims)
                .signWith(key, io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();

        log.debug("JWT токен сгенерирован для пользователя: {}, срок действия: {}", username, expiryDate);
        return token;
    }

    /**
     * Генерирует refresh токен.
     *
     * @param username имя пользователя
     * @return refresh токен
     */
    public String generateRefreshToken(String username) {
        log.debug("Генерация refresh токена для пользователя: {}", username);

        var now = Instant.now();
        var expiryDate = Date.from(now.plusMillis(refreshExpirationMs));

        var token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(expiryDate)
                .signWith(key, io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();

        log.debug("Refresh токен сгенерирован для пользователя: {}, срок действия: {}", username, expiryDate);
        return token;
    }

    /**
     * Получить имя пользователя из JWT токена.
     *
     * @param token JWT токен
     * @return имя пользователя
     */
    public String getUsernameFromToken(String token) {
        try {
            var claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (JwtException e) {
            log.error("Ошибка при извлечении имени пользователя из токена: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Получить роли из JWT токена.
     *
     * @param token JWT токен
     * @return список ролей
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        try {
            var claims = getClaimsFromToken(token);
            return claims.get("roles", List.class);
        } catch (JwtException e) {
            log.error("Ошибка при извлечении ролей из токена: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Проверяет валидность JWT токена.
     *
     * @param token JWT токен
     * @return true если токен валиден
     */
    public boolean validateToken(String token) {
        try {
            log.debug("Валидация JWT токена");

            getClaimsFromToken(token);

            log.debug("JWT токен валиден");
            return true;

        } catch (SignatureException e) {
            log.error("Невалидная подпись JWT токена: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Неверный формат JWT токена: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT токен истек: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Неподдерживаемый JWT токен: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Пустой JWT токен: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Проверяет, истек ли токен.
     *
     * @param token JWT токен
     * @return true если токен истек
     */
    public boolean isTokenExpired(String token) {
        try {
            var claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            log.error("Ошибка при проверке истечения токена: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Получить время истечения токена.
     *
     * @param token JWT токен
     * @return дата истечения
     */
    public Date getExpirationDate(String token) {
        try {
            var claims = getClaimsFromToken(token);
            return claims.getExpiration();
        } catch (JwtException e) {
            log.error("Ошибка при получении даты истечения токена: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Создает объект Authentication из JWT токена.
     *
     * @param token JWT токен
     * @return Authentication объект
     */
    public Authentication getAuthentication(String token) {
        log.debug("Создание Authentication из токена");

        var username = getUsernameFromToken(token);
        var roles = getRolesFromToken(token);

        var authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        var principal = new User(username, "", authorities);

        log.debug("Authentication создан для пользователя: {}", username);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * Обновляет токен (генерирует новый на основе существующего).
     *
     * @param token старый JWT токен
     * @return новый JWT токен
     */
    public String refreshToken(String token) {
        log.debug("Обновление JWT токена");

        var username = getUsernameFromToken(token);
        var roles = getRolesFromToken(token);

        if (username == null) {
            log.error("Не удалось обновить токен: имя пользователя не найдено");
            throw new IllegalArgumentException("Не удалось обновить токен");
        }

        var newToken = generateToken(username, roles);
        log.debug("JWT токен обновлен для пользователя: {}", username);

        return newToken;
    }

    /**
     * Проверяет, что токен принадлежит указанному пользователю.
     *
     * @param token    JWT токен
     * @param username имя пользователя
     * @return true если токен принадлежит пользователю
     */
    public boolean isValidForUser(String token, String username) {
        var tokenUsername = getUsernameFromToken(token);
        return username.equals(tokenUsername) && validateToken(token);
    }

    // ============================================================
    // ПРИВАТНЫЙ МЕТОД ДЛЯ ИЗВЛЕЧЕНИЯ CLAIMS
    // ============================================================

    /**
     * Извлекает Claims из JWT токена.
     * Использует проверенный API, который работает во всех версиях jjwt 0.11.x.
     *
     * @param token JWT токен
     * @return Claims
     * @throws JwtException если токен невалиден
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}