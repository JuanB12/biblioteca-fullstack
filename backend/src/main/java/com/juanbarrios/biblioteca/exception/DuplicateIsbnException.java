package com.juanbarrios.biblioteca.exception;

import org.springframework.http.HttpStatus;

public class DuplicateIsbnException extends BusinessException {

    public DuplicateIsbnException(String isbn) {
        super("Ya existe un libro registrado con el ISBN: " + isbn, HttpStatus.CONFLICT);
    }
}
