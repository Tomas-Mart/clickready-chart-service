package com.clickready.chart.infrastructure.config;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * Конфигурация асинхронной обработки.
 *
 * <p>Настраивает пул потоков для выполнения асинхронных задач:
 * <ul>
 *   <li>Основной пул для бизнес-операций</li>
 *   <li>Отдельный пул для длительных задач</li>
 *   <li>Обработка ошибок в асинхронных методах</li>
 * </ul>
 *
 * @author ClickReady Team
 * @version 1.0.0
 * @since 2026-07-01
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Основной пул потоков для асинхронных задач.
     */
    @Bean(name = "taskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        log.info("Инициализация основного пула потоков");

        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("async-thread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        log.info("Пул потоков создан: core={}, max={}, queue={}",
                executor.getCorePoolSize(),
                executor.getMaxPoolSize(),
                executor.getQueueCapacity()
        );

        return executor;
    }

    /**
     * Пул потоков для длительных задач.
     */
    @Bean(name = "longTaskExecutor")
    public Executor longTaskExecutor() {
        log.info("Инициализация пула для длительных задач");

        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(120);
        executor.setThreadNamePrefix("long-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.initialize();

        return executor;
    }

    /**
     * Пул потоков для Kafka потребителей.
     */
    @Bean(name = "kafkaExecutor")
    public Executor kafkaExecutor() {
        log.info("Инициализация пула для Kafka");

        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(15);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("kafka-thread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();

        return executor;
    }

    /**
     * Обработчик необработанных исключений в асинхронных методах.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {
            @Override
            public void handleUncaughtException(Throwable ex, Method method, Object... params) {
                log.error("Необработанное исключение в асинхронном методе: {}.{} с параметрами: {}",
                        method.getDeclaringClass().getName(),
                        method.getName(),
                        Arrays.toString(params),
                        ex
                );

                // Здесь можно отправить уведомление в систему мониторинга
                // notificationService.sendAlert("Async error in " + method.getName(), ex);
            }
        };
    }
}