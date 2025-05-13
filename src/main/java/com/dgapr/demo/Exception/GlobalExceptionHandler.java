package com.dgapr.demo.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice // This makes it a global exception handler
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Handle general exceptions not caught by specific handlers
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred: " + ex.getMessage()); // Avoid leaking sensitive details in production
        body.put("path", request.getDescription(false)); // Includes request path

        // Log the error server-side
        logger.error("Unhandled exception: " + ex.getMessage(), ex); // Use logger

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Example handler for a custom or specific application exception
    // If you have custom exceptions like ResourceNotFoundException, handle them here
    // @ExceptionHandler(ResourceNotFoundException.class)
    // public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
    //     Map<String, Object> body = new LinkedHashMap<>();
    //     body.put("timestamp", LocalDateTime.now());
    //     body.put("status", HttpStatus.NOT_FOUND.value());
    //     body.put("error", "Not Found");
    //     body.put("message", ex.getMessage());
    //     body.put("path", request.getDescription(false));
    //     return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    // }

    // You can override methods from ResponseEntityExceptionHandler
    // to customize handling of Spring MVC exceptions like MethodArgumentNotValidException (for @Valid)
    // or HttpRequestMethodNotSupportedException etc.

    // Example for handling IllegalArgumentException explicitly if not caught in controller
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // Add handlers for DataIntegrityViolationException if needed for more specific messages
    // (The service already throws IllegalArgumentException for it, which is caught above)
}