package com.juanbarrios.biblioteca.web.dto;

import jakarta.validation.constraints.NotNull;

public record ReservationCreateRequest(
        @NotNull(message = "el libro es obligatorio")
        Long bookId) {
}
