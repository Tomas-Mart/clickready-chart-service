package com.clickready.chart.infrastructure.config;

import java.time.Duration;
import java.util.HashMap;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

/**
 * Конфигурация кэширования для Redis.
 *
 * <p>Настраивает:
 * <ul>
 *   <li>CacheManager для Spring Cache абстракции</li>
 *   <li>RedisTemplate для прямого доступа к Redis</li>
 *   <li>Jackson сериализацию для хранения объектов</li>
 *   <li>Индивидуальные TTL для разных кэшей</li>
 * </ul>
 *
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Создает и настраивает CacheManager для Redis.
     *
     * <p>Использует Jackson для сериализации объектов в JSON.
     * Поддерживает LocalDate, LocalDateTime через JavaTimeModule.
     *
     * @param redisConnectionFactory фабрика подключения к Redis
     * @return настроенный CacheManager
     */
    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        log.info("Инициализация Redis Cache Manager с фабрикой: {}", redisConnectionFactory);

        // ✅ Проверка подключения к Redis
        try {
            var connection = redisConnectionFactory.getConnection();
            log.info("✅ Redis подключение успешно установлено");
            connection.close();
        } catch (Exception e) {
            log.error("❌ Ошибка подключения к Redis: {}", e.getMessage());
            log.warn("⚠️ Кэш будет работать в режиме без Redis (fallback)");
        }

        // Настройка ObjectMapper для работы с Java 8 Time API
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        var serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Базовая конфигурация кэша
        var defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                );

        // Индивидуальные настройки для каждого кэша
        var cacheConfigurations = new HashMap<String, RedisCacheConfiguration>();
        cacheConfigurations.put("chartData", defaultConfig.entryTtl(Duration.ofSeconds(60)));
        cacheConfigurations.put("chartDataRange", defaultConfig.entryTtl(Duration.ofSeconds(60)));
        cacheConfigurations.put("externalApi", defaultConfig.entryTtl(Duration.ofSeconds(300)));
        cacheConfigurations.put("externalApiData", defaultConfig.entryTtl(Duration.ofSeconds(300)));

        log.info("✅ Redis Cache Manager инициализирован с {} кэшами", cacheConfigurations.size());

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .enableStatistics()
                .build();
    }

    /**
     * Создает и настраивает RedisTemplate для прямого доступа к Redis.
     *
     * <p>Используется для операций, не покрываемых Spring Cache абстракцией.
     *
     * @param redisConnectionFactory фабрика подключения к Redis
     * @return настроенный RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("Инициализация RedisTemplate с фабрикой: {}", redisConnectionFactory);

        // ✅ Проверка подключения к Redis
        try {
            var connection = redisConnectionFactory.getConnection();
            log.info("✅ RedisTemplate подключение успешно установлено");
            connection.close();
        } catch (Exception e) {
            log.error("❌ Ошибка подключения RedisTemplate: {}", e.getMessage());
        }

        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        var template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        template.afterPropertiesSet();

        log.info("✅ RedisTemplate инициализирован");

        return template;
    }
}