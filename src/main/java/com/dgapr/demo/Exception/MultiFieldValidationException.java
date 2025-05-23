package com.dgapr.demo.Exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class MultiFieldValidationException extends RuntimeException {
    private final Map<String, String> errors;

    public MultiFieldValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }

}

