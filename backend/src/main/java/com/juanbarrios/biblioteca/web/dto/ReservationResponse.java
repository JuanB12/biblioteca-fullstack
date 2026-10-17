package com.juanbarrios.biblioteca.web.dto;

import com.juanbarrios.biblioteca.domain.Reservation;
import com.juanbarrios.biblioteca.domain.ReservationStatus;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long bookId,
        String bookTitle,
        String requesterEmail,
        LocalDateTime requestDate,
        ReservationStatus status) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getBook().getId(),
                reservation.getBook().getTitle(),
                reservation.getRequesterEmail(),
                reservation.getRequestDate(),
                reservation.getStatus());
    }
}
