package com.juanbarrios.biblioteca.exception;

import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends BusinessException {

    public DuplicateEmailException(String email) {
        super("Ya existe una cuenta registrada con el correo: " + email, HttpStatus.CONFLICT);
    }
}
