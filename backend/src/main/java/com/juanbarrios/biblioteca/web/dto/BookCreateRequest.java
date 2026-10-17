package com.juanbarrios.biblioteca.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record BookCreateRequest(
        @NotBlank(message = "el titulo es obligatorio")
        String title,

        @NotBlank(message = "el autor es obligatorio")
        String author,

        @NotBlank(message = "el ISBN es obligatorio")
        String isbn,

        @Positive(message = "el anio de publicacion debe ser positivo")
        Integer publicationYear,

        String coverUrl,

        String subjects) {
}
