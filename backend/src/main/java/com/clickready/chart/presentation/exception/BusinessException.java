package com.clickready.chart.presentation.exception;

import lombok.Getter;

/**
 * Исключение для бизнес-ошибок.
 * Содержит код ошибки для удобной обработки на клиенте.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final String userMessage;

    /**
     * Конструктор с сообщением и кодом ошибки.
     */
    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = message;
    }

    /**
     * Конструктор с сообщением, кодом и причиной.
     */
    public BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userMessage = message;
    }

    /**
     * Конструктор с сообщением и дефолтным кодом.
     */
    public BusinessException(String message) {
        this(message, "BUSINESS_ERROR");
    }

    /**
     * Конструктор с сообщением для пользователя и техническим сообщением.
     */
    public BusinessException(String userMessage, String technicalMessage, String errorCode) {
        super(technicalMessage);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }
}