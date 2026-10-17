package com.juanbarrios.biblioteca.exception;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

public class UserBlockedException extends BusinessException {

    public UserBlockedException(LocalDateTime blockedUntil) {
        super(buildMessage(blockedUntil), HttpStatus.CONFLICT);
    }

    private static String buildMessage(LocalDateTime blockedUntil) {
        if (blockedUntil == null) {
            return "La cuenta esta bloqueada para pedir prestamos hasta que un administrador la desbloquee";
        }
        return "La cuenta esta bloqueada para pedir prestamos hasta " + blockedUntil;
    }
}
