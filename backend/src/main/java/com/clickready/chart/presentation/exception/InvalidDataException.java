package com.clickready.chart.presentation.exception;

import lombok.Getter;

/**
 * Исключение для невалидных данных.
 * Используется при нарушении бизнес-валидации.
 */
@Getter
public class InvalidDataException extends RuntimeException {

    private final String fieldName;
    private final Object fieldValue;
    private final String userMessage;

    /**
     * Полный конструктор с деталями.
     */
    public InvalidDataException(String fieldName, Object fieldValue, String userMessage) {
        super(String.format("Невалидное значение поля %s: '%s' - %s", fieldName, fieldValue, userMessage));
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.userMessage = userMessage;
    }

    /**
     * Конструктор для простых случаев.
     */
    public InvalidDataException(String message) {
        this(null, null, message);
    }

    /**
     * Конструктор с причиной.
     */
    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
        this.fieldName = null;
        this.fieldValue = null;
        this.userMessage = message;
    }
}