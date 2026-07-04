package com.clickready.chart.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@TestConfiguration
@SuppressWarnings({"resource", "unused"})
public class TestcontainersConfig {

    private static final String POSTGRES_IMAGE = "postgres:16-alpine";
    private static final String REDIS_IMAGE = "redis:7-alpine";

    private static final String DATABASE_NAME = "testdb";
    private static final String DATABASE_USER = "testuser";
    private static final String DATABASE_PASSWORD = "testpass";

    @Bean
    @Primary
    @SuppressWarnings("resource")  // ✅ Подавляем предупреждение для этого метода
    public PostgreSQLContainer<?> postgresContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_IMAGE))
                .withDatabaseName(DATABASE_NAME)
                .withUsername(DATABASE_USER)
                .withPassword(DATABASE_PASSWORD)
                .withReuse(true);
        container.start();

        // ✅ Устанавливаем свойства для подключения
        System.setProperty("DB_URL", container.getJdbcUrl());
        System.setProperty("DB_USERNAME", DATABASE_USER);
        System.setProperty("DB_PASSWORD", DATABASE_PASSWORD);

        log.info("PostgreSQL started at: {}", container.getJdbcUrl());

        return container;
    }

    @Bean
    @SuppressWarnings("resource")
    public GenericContainer<?> redisContainer() {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
                .withExposedPorts(6379)
                .withReuse(true);
        container.start();

        // ✅ Для Redis (если используется)
        System.setProperty("REDIS_HOST", container.getHost());
        System.setProperty("REDIS_PORT", String.valueOf(container.getMappedPort(6379)));

        log.info("Redis started at: {}:{}", container.getHost(), container.getMappedPort(6379));

        return container;
    }
}