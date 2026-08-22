package com.algomentor.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "validation_failed");
        body.put("status", 400);
        body.put("fields", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }


    @ExceptionHandler(InvalidModelOutputException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidModelOutput(InvalidModelOutputException ex) {
        log.warn("Invalid model output: {}", ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "invalid_model_output");
        body.put("status", 502);
        body.put("message", "Unable to generate a valid analysis right now. Please retry.");

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    @ExceptionHandler(LlmUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleLlmUnavailable(LlmUnavailableException ex) {
        log.warn("LLM provider unavailable: {}", ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "llm_unavailable");
        body.put("status", 503);
        body.put("message", ex.getUserMessage());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoResourceFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "not_found");
        body.put("status", 404);
        body.put("path", ex.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unhandled API exception", ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "internal_error");
        body.put("status", 500);
        body.put("message", "Unexpected server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
