package com.pruebatecnica.pruebatecnica.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class MyControllerAdvice {

    private static final String MESSAGE_KEY = "message";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<Map<String, String>>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {

        List<Map<String, String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", error.getDefaultMessage()))
                .toList();

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ResponseStatusException.class)
    private ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {

        HttpStatus httpStatus = (HttpStatus) ex.getStatusCode();
        Map<String, Object> error = new HashMap<>();
        error.put(MESSAGE_KEY, ex.getReason());
        return new ResponseEntity<>(error, httpStatus);
    }

    @ExceptionHandler(Exception.class)
    private ResponseEntity<Map<String, Object>> handleException(Exception ex) {

        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        Map<String, Object> error = new HashMap<>();
        if (ex.getMessage() != null) {
            error.put(MESSAGE_KEY, "Ha ocurrido un error interno. Contacta al administrador");
            error.put("timestamp", LocalDateTime.now());
            // error.put("messageError", ex.getMessage());
        }

        return new ResponseEntity<>(error, httpStatus);
    }
}