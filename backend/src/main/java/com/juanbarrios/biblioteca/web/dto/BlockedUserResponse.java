package com.juanbarrios.biblioteca.web.dto;

import com.juanbarrios.biblioteca.domain.AppUser;
import java.time.LocalDateTime;

public record BlockedUserResponse(
        Long id,
        String name,
        String email,
        LocalDateTime blockedUntil) {

    public static BlockedUserResponse from(AppUser user) {
        return new BlockedUserResponse(user.getId(), user.getName(), user.getEmail(), user.getBlockedUntil());
    }
}
