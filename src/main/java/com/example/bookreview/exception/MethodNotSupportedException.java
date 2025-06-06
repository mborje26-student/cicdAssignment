package com.example.bookreview.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
public class MethodNotSupportedException extends RuntimeException {
    public MethodNotSupportedException(String message) {
        super(message);
    }
}
