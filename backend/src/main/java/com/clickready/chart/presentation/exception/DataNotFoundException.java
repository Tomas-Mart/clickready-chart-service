package com.clickready.chart.presentation.exception;

import lombok.Getter;

/**
 * Исключение, выбрасываемое когда запрашиваемый ресурс не найден.
 * Содержит детальную информацию для логирования и API клиентов.
 */
@Getter
public class DataNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;
    private final String userMessage;

    /**
     * Полный конструктор с деталями.
     */
    public DataNotFoundException(String resourceName, String fieldName, Object fieldValue, String userMessage) {
        super(String.format("%s с %s='%s' не найден", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.userMessage = userMessage;
    }

    /**
     * Упрощенный конструктор для стандартных случаев.
     */
    public DataNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        this(resourceName, fieldName, fieldValue,
                String.format("%s с %s='%s' не найден", resourceName, fieldName, fieldValue));
    }

    /**
     * Конструктор только с сообщением.
     */
    public DataNotFoundException(String message) {
        this("Resource", "id", "unknown", message);
    }
}