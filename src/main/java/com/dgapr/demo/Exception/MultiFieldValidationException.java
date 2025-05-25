package com.dgapr.demo.Exception;

import lombok.Getter;

import java.util.Map;

@Getter
/**
 * Exception thrown when multiple field validation errors occur.
 * Contains a map of field names to their corresponding error messages.
 */
public class MultiFieldValidationException extends RuntimeException {

    private final Map<String, String> errors;

    /**
     * Constructs a new MultiFieldValidationException with the specified detail message and errors map.
     *
     * @param message the detail message
     * @param errors  the map of field errors
     */
    public MultiFieldValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }

}

