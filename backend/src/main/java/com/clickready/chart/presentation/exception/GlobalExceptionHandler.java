package com.clickready.chart.presentation.exception;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Глобальный обработчик исключений.
 * Реализует стандарт Problem Details (RFC 7807).
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_PATH = "https://api.clickready.com/errors/";

    /**
     * Обработка исключений валидации (для @Valid в контроллерах).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {

        log.error("Ошибка валидации: {}", ex.getMessage());

        var errors = new HashMap<String, String>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            var fieldName = switch (error) {
                case FieldError fe -> fe.getField();
                default -> error.getObjectName();
            };
            errors.put(fieldName, error.getDefaultMessage());
        });

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Ошибка валидации данных");
        problem.setDetail("Проверьте корректность введенных данных");
        problem.setType(URI.create("https://api.clickready.com/errors/validation"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errors", errors);

        return ResponseEntity.badRequest().body(problem);
    }

    /**
     * Обработка ConstraintViolationException (для валидации параметров).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolationException(
            ConstraintViolationException ex,
            WebRequest request
    ) {

        log.error("Ошибка валидации параметров: {}", ex.getMessage());

        var errors = new HashMap<String, String>();
        ex.getConstraintViolations().forEach(violation -> {
            var fieldName = violation.getPropertyPath().toString();
            var message = violation.getMessage();
            errors.put(fieldName, message);
        });

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Ошибка валидации параметров");
        problem.setDetail("Проверьте корректность переданных параметров");
        problem.setType(URI.create("https://api.clickready.com/errors/constraint-violation"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errors", errors);

        return ResponseEntity.badRequest().body(problem);
    }

    /**
     * Обработка исключений бизнес-логики.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusinessException(
            BusinessException ex,
            WebRequest request
    ) {

        log.error("Бизнес-ошибка: {}", ex.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Бизнес-ошибка");
        problem.setDetail(ex.getMessage());
        problem.setType(URI.create("https://api.clickready.com/errors/business"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errorCode", ex.getErrorCode());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    /**
     * Обработка исключений "данные не найдены".
     */
    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleDataNotFoundException(
            DataNotFoundException ex,
            WebRequest request
    ) {
        log.warn("Ресурс не найден: ресурс={}, поле={}, значение={}, сообщение={}",
                ex.getResourceName(),
                ex.getFieldName(),
                ex.getFieldValue(),
                ex.getUserMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Ресурс не найден");
        problem.setDetail(ex.getUserMessage());
        problem.setType(URI.create("https://api.clickready.com/errors/not-found"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("resource", ex.getResourceName());
        problem.setProperty("field", ex.getFieldName());
        problem.setProperty("value", ex.getFieldValue());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    /**
     * Обработка InvalidDataException (невалидные данные).
     */
    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ProblemDetail> handleInvalidDataException(
            InvalidDataException ex,
            WebRequest request
    ) {
        log.warn("Невалидные данные: {}", ex.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Невалидные данные");
        problem.setDetail(ex.getUserMessage() != null ? ex.getUserMessage() : ex.getMessage());
        problem.setType(URI.create("https://api.clickready.com/errors/invalid-data"));
        problem.setProperty("timestamp", Instant.now());

        if (ex.getFieldName() != null) {
            problem.setProperty("field", ex.getFieldName());
            problem.setProperty("value", ex.getFieldValue());
        }

        return ResponseEntity.badRequest().body(problem);
    }

    /**
     * Обработка IllegalArgumentException.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request
    ) {

        log.error("Неверный аргумент: {}", ex.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Неверный аргумент");
        problem.setDetail(ex.getMessage());
        problem.setType(URI.create("https://api.clickready.com/errors/illegal-argument"));
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.badRequest().body(problem);
    }

    /**
     * Обработка ошибок целостности данных (уникальность, foreign key и т.д.).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            WebRequest request
    ) {

        log.error("Ошибка целостности данных: {}", ex.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Конфликт данных");
        problem.setDetail("Нарушение целостности данных. Возможно, запись уже существует.");
        problem.setType(URI.create("https://api.clickready.com/errors/data-integrity"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("message", ex.getMostSpecificCause().getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    /**
     * Обработка общих исключений.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(
            Exception ex,
            WebRequest request
    ) {

        log.error("Внутренняя ошибка сервера: {}", ex.getMessage(), ex);

        var problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Внутренняя ошибка сервера");
        problem.setDetail("Произошла непредвиденная ошибка. Попробуйте позже.");
        problem.setType(URI.create("https://api.clickready.com/errors/internal"));
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.internalServerError().body(problem);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            WebRequest request
    ) {

        log.warn("Неверный тип аргумента: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Неверный формат параметра: " + ex.getName()
        );
        problemDetail.setType(URI.create(ERROR_PATH + "bad-request"));
        problemDetail.setTitle("Неверный запрос");
        problemDetail.setProperty("parameter", ex.getName());
        problemDetail.setProperty("expected", ex.getRequiredType() != null ?
                ex.getRequiredType().getSimpleName() : "unknown");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));

        return ResponseEntity.badRequest().body(problemDetail);
    }
}