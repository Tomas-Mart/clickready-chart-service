package com.clickready.chart.infrastructure.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;

/**
 * Конфигурация Swagger/OpenAPI документации.
 *
 * <p>Настраивает:
 * <ul>
 *   <li>Информацию о API</li>
 *   <li>JWT аутентификацию</li>
 *   <li>Серверы для разных окружений</li>
 * </ul>
 *
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
@Slf4j
@Configuration
public class SwaggerConfig {

    /**
     * Настройка OpenAPI документации.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        log.info("Инициализация OpenAPI документации");

        var openApi = new OpenAPI()
                .info(createApiInfo())
                .servers(createServers())
                .components(createComponents())
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));

        log.info("OpenAPI документация создана");

        return openApi;
    }

    /**
     * Создает информацию об API.
     */
    private Info createApiInfo() {
        return new Info()
                .title("ClickReady Chart Service API")
                .version("1.0.0")
                .description("""
                        Микросервис для управления данными графика ClickReady.
                        
                        ## Основные возможности:
                        - Получение данных графика
                        - Создание и обновление записей
                        - Пакетная обработка данных
                        - Удаление по дате
                        
                        ## Аутентификация:
                        Используется JWT токен. Добавьте в заголовок:
                        `Authorization: Bearer <your-token>`
                        """)
                .contact(new Contact()
                        .name("ClickReady Team")
                        .email("dev@clickready.com")
                        .url("https://clickready.com")
                )
                .license(new License()
                        .name("Proprietary")
                        .url("https://clickready.com/legal")
                )
                .termsOfService("https://clickready.com/terms");
    }

    /**
     * Создает список серверов.
     */
    private List<Server> createServers() {
        return List.of(
                new Server()
                        .url("http://localhost:8080")
                        .description("Локальная разработка"),
                new Server()
                        .url("https://dev-api.clickready.com")
                        .description("Dev окружение"),
                new Server()
                        .url("https://staging-api.clickready.com")
                        .description("Staging окружение"),
                new Server()
                        .url("https://api.clickready.com")
                        .description("Production окружение")
        );
    }

    /**
     * Создает компоненты для безопасности.
     */
    private Components createComponents() {
        return new Components()
                .addSecuritySchemes("Bearer Authentication", createSecurityScheme());
    }

    /**
     * Создает схему безопасности (JWT).
     */
    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("""
                        Введите JWT токен.
                        
                        Пример: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
                        
                        Для получения токена используйте эндпоинт `/api/v1/auth/login`
                        """)
                .name("Authorization")
                .in(SecurityScheme.In.HEADER);
    }
}