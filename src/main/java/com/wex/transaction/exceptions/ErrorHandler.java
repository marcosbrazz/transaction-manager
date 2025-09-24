package com.wex.transaction.exceptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

@ControllerAdvice
public class ErrorHandler {
    
    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);
    private static final Map<String, String> formatErrorsMessage = Map.of(
        "transactionDate", "Invalid date format. Expected yyyy-MM-dd",
        "amount", "Invalid decimal format."
    );

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.debug("Validation error handled: {}", ex.getMessage());
        List<Map<String, String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> {
                    Map<String, String> err = new HashMap<>();
                    err.put("field", error.getField());
                    err.put("message", error.getDefaultMessage());
                    return err;
                })
                .collect(Collectors.toList());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("errors", errors);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidDateFormat(HttpMessageNotReadableException ex) {
        log.debug("Invalid request error handled: {}", ex.getMessage());
        Throwable cause = ex.getCause();
        Map<String, Object> errorResponse = new HashMap<>();
        if (cause instanceof InvalidFormatException invalidFormatEx) {
            String fieldName = invalidFormatEx.getPath().isEmpty()
                ? "unknownField"
                : invalidFormatEx.getPath().get(0).getFieldName();
            Map<String, String> error = Map.of(
                "field", fieldName,
                "message", formatErrorsMessage.get(fieldName)
            );
            errorResponse.put("errors", List.of(error));
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        Map<String, Object> error = new HashMap<>();
        error.put("message", "Malformed JSON request");
        errorResponse.put("errors", List.of(error));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PurchaseNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePurchaseNotFound(PurchaseNotFoundException ex) {
        return this.handleError(HttpStatus.NOT_FOUND, ex);
    }
    
    @ExceptionHandler(InternalApplicationError.class)
    public ResponseEntity<Map<String, Object>> handleInternalErrors(InternalApplicationError ex) {
        return this.handleError(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<Map<String, Object>> handleApplicationErrors(ApplicationException ex) {
        return this.handleError(HttpStatus.BAD_REQUEST, ex);
    }

    private ResponseEntity<Map<String, Object>> handleError(HttpStatus status, ApplicationException ex) {
        log.debug("Error handled: {}", ex.getMessage());
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("errors", List.of(ex.getAttributes()));
        return new ResponseEntity<>(errorResponse, status);
    }
}
