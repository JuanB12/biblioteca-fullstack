package com.juanbarrios.biblioteca.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "el nombre es obligatorio")
        String name,

        @NotBlank(message = "el correo es obligatorio")
        @Email(message = "el correo no tiene un formato valido")
        String email,

        @NotBlank(message = "la contrasena es obligatoria")
        @Size(min = 8, message = "la contrasena debe tener al menos 8 caracteres")
        String password) {
}
