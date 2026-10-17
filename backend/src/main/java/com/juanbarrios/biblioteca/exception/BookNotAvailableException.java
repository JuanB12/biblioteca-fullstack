package com.juanbarrios.biblioteca.exception;

import org.springframework.http.HttpStatus;

public class BookNotAvailableException extends BusinessException {

    public BookNotAvailableException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
