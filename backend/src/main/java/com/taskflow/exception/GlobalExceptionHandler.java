package com.taskflow.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles @Valid validation failures → 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());
        Map<String, String> fields = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fields.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("error", "validation failed");
        body.put("fields", fields);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // Handles all RuntimeExceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(
            RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage());
        String message = ex.getMessage();

        if (message == null) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
        }

        return switch (message) {
            case "FORBIDDEN"         -> error(HttpStatus.FORBIDDEN, "forbidden");
            case "Project not found" -> error(HttpStatus.NOT_FOUND, "not found");
            case "Task not found"    -> error(HttpStatus.NOT_FOUND, "not found");
            case "Assignee not found"-> error(HttpStatus.NOT_FOUND, "not found");
            case "Email already in use" -> error(HttpStatus.BAD_REQUEST, message);
            case "Invalid email or password" -> error(HttpStatus.UNAUTHORIZED, message);
            default -> error(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
        };
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }
}