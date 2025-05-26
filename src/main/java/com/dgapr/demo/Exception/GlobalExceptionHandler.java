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

    /**
    * Handles MultiFieldValidationException and returns a 400 Bad Request response.
    *
    * @param ex the MultiFieldValidationException thrown
    * @return a ResponseEntity containing error details, validation errors, and HTTP 400 status
    */
    @ExceptionHandler(com.dgapr.demo.Exception.MultiFieldValidationException.class)
    public ResponseEntity<Object> handleMultiFieldValidationException(com.dgapr.demo.Exception.MultiFieldValidationException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put("message", ex.getMessage());
        body.put("errors", ex.getErrors());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
    * Handles IllegalArgumentException and returns a 400 Bad Request response.
    *
    * @param ex the IllegalArgumentException thrown
    * @return a ResponseEntity containing error details and HTTP 400 status
    */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles DuplicateCertificateException, returning a 409 Conflict status.
     * This indicates that the request could not be completed due to a conflict
     * with the current state of the target resource.
     *
     * @param ex The DuplicateCertificateException caught.
     * @return A ResponseEntity with a 409 Conflict status and error details.
     */
    @ExceptionHandler(com.dgapr.demo.Exception.DuplicateCertificateException.class)
    public ResponseEntity<Object> handleDuplicateCertificateException(com.dgapr.demo.Exception.DuplicateCertificateException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", HttpStatus.CONFLICT.getReasonPhrase());
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Handles all uncaught exceptions that are not specifically handled by other exception handlers.
     *
     * @param ex the exception that was thrown
     * @param request the current web request
     * @return a ResponseEntity containing error details and HTTP 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred.");
        body.put("path", request.getDescription(false));

        logger.error("Unhandled exception: " + ex.getMessage(), ex);

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}