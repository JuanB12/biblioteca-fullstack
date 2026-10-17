package com.juanbarrios.biblioteca.repository;

import com.juanbarrios.biblioteca.domain.Reservation;
import com.juanbarrios.biblioteca.domain.ReservationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findFirstByBookIdAndStatusOrderByRequestDateAsc(Long bookId, ReservationStatus status);

    List<Reservation> findByRequesterEmailIgnoreCaseOrderByRequestDateDesc(String requesterEmail);

    boolean existsByBookIdAndRequesterEmailIgnoreCaseAndStatus(Long bookId, String requesterEmail, ReservationStatus status);
}
