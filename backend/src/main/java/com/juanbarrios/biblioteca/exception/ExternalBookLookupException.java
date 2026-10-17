package com.juanbarrios.biblioteca.exception;

import org.springframework.http.HttpStatus;

public class ExternalBookLookupException extends BusinessException {

    public ExternalBookLookupException(String message, Throwable cause) {
        super(message, HttpStatus.BAD_GATEWAY);
        initCause(cause);
    }
}
