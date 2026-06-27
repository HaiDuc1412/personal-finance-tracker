package com.haiduc.personalfinancetracker.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s not found with %s: %s", resource, field, value), HttpStatus.NOT_FOUND);
    }
}
