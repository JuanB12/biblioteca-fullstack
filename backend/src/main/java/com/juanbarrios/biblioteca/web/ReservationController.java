package com.juanbarrios.biblioteca.web;

import com.juanbarrios.biblioteca.domain.AppUser;
import com.juanbarrios.biblioteca.domain.Reservation;
import com.juanbarrios.biblioteca.security.AppUserPrincipal;
import com.juanbarrios.biblioteca.service.ReservationService;
import com.juanbarrios.biblioteca.web.dto.ReservationCreateRequest;
import com.juanbarrios.biblioteca.web.dto.ReservationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @Valid @RequestBody ReservationCreateRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        Reservation reservation = reservationService.create(request.bookId(), principal.getUser().getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservationResponse.from(reservation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        AppUser user = principal.getUser();
        reservationService.cancel(id, user.getEmail(), user.getRole());
        return ResponseEntity.noContent().build();
    }
}
