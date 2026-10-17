package com.juanbarrios.biblioteca.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "el correo es obligatorio")
        String email,

        @NotBlank(message = "la contrasena es obligatoria")
        String password) {
}
