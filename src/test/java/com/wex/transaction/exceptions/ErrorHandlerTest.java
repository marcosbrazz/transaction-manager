package com.wex.transaction.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

class ErrorHandlerTest {

    private ErrorHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ErrorHandler();
    }

    @Test
    @SuppressWarnings("null")
    void shouldHandleValidationErrors() {
        // Arrange
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "purchase");
        bindingResult.addError(new FieldError("purchase", "description", "must not exceed 50 characters"));
        bindingResult.addError(new FieldError("purchase", "amount", "must be greater than 0"));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleValidationErrors(ex);

        // Assert
        assertEquals(400, response.getStatusCode().value());
        List<?> errors = (List<?>) response.getBody().get("errors");
        assertEquals(2, errors.size());
    }

    @Test
    @SuppressWarnings({ "null", "unchecked" })
    void shouldHandleInvalidDateFormat() {
        // Arrange
        InvalidFormatException cause = new InvalidFormatException(
                null, "invalid date", "2025-99-99",
                java.time.LocalDate.class
        );
        cause.prependPath(new Object(), "transactionDate");

        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Invalid", cause, null);

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleInvalidDateFormat(ex);

        // Assert
        assertEquals(400, response.getStatusCode().value());
        Map<?, ?> error = ((List<Map<?, ?>>) response.getBody().get("errors")).get(0);
        assertEquals("transactionDate", error.get("field"));
        assertEquals("Invalid date format. Expected yyyy-MM-dd", error.get("message"));
    }

    @Test
    @SuppressWarnings({ "null", "unchecked" })
    void shouldHandleMalformedJsonWhenNoCause() {
        // Arrange
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Malformed JSON", null, null);

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleInvalidDateFormat(ex);

        // Assert
        assertEquals(400, response.getStatusCode().value());
        Map<?, ?> error = ((List<Map<?, ?>>) response.getBody().get("errors")).get(0);
        assertTrue(((String) error.get("message")).contains("Malformed JSON"));
    }

    @Test
    void shouldHandleServiceErrors() {
        // Arrange
        ServiceException ex = new ServiceException("Generic error", null);

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleApplicationErrors(ex);

        // Assert
        assertEquals(400, response.getStatusCode().value());
        @SuppressWarnings("null")
        List<?> errors = (List<?>) response.getBody().get("errors");
        assertEquals(1, errors.size());
    }

    @Test
    void shouldHandlePurchaseErrors() {
        // Arrange
        ApplicationException ex = new DuplicatePurchaseException("1234");

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleApplicationErrors(ex);

        // Assert
        assertEquals(400, response.getStatusCode().value());
        @SuppressWarnings("null")
        List<?> errors = (List<?>) response.getBody().get("errors");
        assertEquals(1, errors.size());
        assertEquals("1234", ((Map<?, ?>) errors.get(0)).get("id"));
    }
}
