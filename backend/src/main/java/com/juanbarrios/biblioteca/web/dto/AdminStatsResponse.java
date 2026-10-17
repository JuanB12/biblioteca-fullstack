package com.juanbarrios.biblioteca.web.dto;

public record AdminStatsResponse(
        long activeLoans,
        long overdueLoans,
        long availableBooks,
        long borrowedBooks,
        long reservedBooks,
        long blockedUsers) {
}
