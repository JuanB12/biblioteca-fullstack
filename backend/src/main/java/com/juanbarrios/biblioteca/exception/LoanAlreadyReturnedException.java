package com.juanbarrios.biblioteca.exception;

import org.springframework.http.HttpStatus;

public class LoanAlreadyReturnedException extends BusinessException {

    public LoanAlreadyReturnedException(Long loanId) {
        super("El prestamo " + loanId + " ya fue devuelto", HttpStatus.CONFLICT);
    }
}
