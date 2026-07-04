package com.clickready.chart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import lombok.extern.slf4j.Slf4j;

/**
 * Главный класс приложения ClickReady Chart Service.
 *
 * <p>Микросервис для управления данными графика с поддержкой:
 * <ul>
 *   <li>Clean Architecture</li>
 *   <li>Domain-Driven Design</li>
 *   <li>Кэширование (Redis)</li>
 *   <li>Асинхронная обработка</li>
 *   <li>Метрики и мониторинг</li>
 * </ul>
 *
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
@Slf4j
@EnableAsync
@EnableCaching
@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
public class ClickreadyChartApplication {

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        var context = SpringApplication.run(ClickreadyChartApplication.class, args);

        var environment = context.getEnvironment();
        var port = environment.getProperty("server.port", "8080");
        var activeProfiles = String.join(", ", environment.getActiveProfiles());

        log.info("""
                
                ╔══════════════════════════════════════════════════════════════╗
                ║  🚀 ClickReady Chart Service успешно запущен!              ║
                ║                                                             ║
                ║  📊 Порт: {}                                           ║
                ║  🌍 Профили: {}                                            ║
                ║  📝 Swagger: http://localhost:{}/swagger-ui.html          ║
                ║  📈 Actuator: http://localhost:{}/actuator/health         ║
                ║  🔥 Prometheus: http://localhost:{}/actuator/prometheus   ║
                ╚══════════════════════════════════════════════════════════════╝
                """, port, activeProfiles, port, port, port);
    }
}