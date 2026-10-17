package com.juanbarrios.biblioteca.web.dto;

public record AuthResponse(
        String token,
        String name,
        String email,
        String role) {
}
