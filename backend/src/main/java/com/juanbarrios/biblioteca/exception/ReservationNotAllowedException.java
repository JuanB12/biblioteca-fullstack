package com.juanbarrios.biblioteca.exception;

import org.springframework.http.HttpStatus;

public class ReservationNotAllowedException extends BusinessException {

    public ReservationNotAllowedException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
