package com.juanbarrios.biblioteca.service;

import com.juanbarrios.biblioteca.domain.Book;
import com.juanbarrios.biblioteca.domain.BookStatus;
import com.juanbarrios.biblioteca.domain.Reservation;
import com.juanbarrios.biblioteca.domain.ReservationStatus;
import com.juanbarrios.biblioteca.domain.Role;
import com.juanbarrios.biblioteca.event.BookAvailableEvent;
import com.juanbarrios.biblioteca.exception.ResourceNotFoundException;
import com.juanbarrios.biblioteca.exception.ReservationNotAllowedException;
import com.juanbarrios.biblioteca.repository.BookRepository;
import com.juanbarrios.biblioteca.repository.ReservationRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Reservation create(Long bookId, String requesterEmail) {
        Book book = bookService.getByIdOrThrow(bookId);

        if (book.getStatus() == BookStatus.DISPONIBLE) {
            throw new ReservationNotAllowedException(
                    "El libro esta disponible ahora mismo, no hace falta reservarlo: pidelo directamente");
        }

        if (reservationRepository.existsByBookIdAndRequesterEmailIgnoreCaseAndStatus(
                bookId, requesterEmail, ReservationStatus.PENDIENTE)) {
            throw new ReservationNotAllowedException("Ya tienes una reserva pendiente para este libro");
        }

        Reservation reservation = Reservation.builder()
                .book(book)
                .requesterEmail(requesterEmail)
                .status(ReservationStatus.PENDIENTE)
                .build();

        return reservationRepository.save(reservation);
    }

    @Transactional
    public void cancel(Long reservationId, String requesterEmail, Role requesterRole) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una reserva con id: " + reservationId));

        boolean isOwner = reservation.getRequesterEmail().equalsIgnoreCase(requesterEmail);
        if (!isOwner && requesterRole != Role.ADMIN) {
            throw new AccessDeniedException("No puedes cancelar una reserva de otra persona");
        }

        if (reservation.getStatus() == ReservationStatus.CUMPLIDO) {
            throw new ReservationNotAllowedException("La reserva ya fue completada, no se puede cancelar");
        }
        if (reservation.getStatus() == ReservationStatus.CANCELADO) {
            throw new ReservationNotAllowedException("La reserva ya estaba cancelada");
        }

        boolean wasNotified = reservation.getStatus() == ReservationStatus.NOTIFICADO;
        reservation.setStatus(ReservationStatus.CANCELADO);
        reservationRepository.save(reservation);

        if (wasNotified) {
            resolveNextInQueueOrRelease(reservation.getBook());
        }
    }

    @Transactional
    public void resolveNextInQueueOrRelease(Book book) {
        Optional<Reservation> next = reservationRepository
                .findFirstByBookIdAndStatusOrderByRequestDateAsc(book.getId(), ReservationStatus.PENDIENTE);

        if (next.isPresent()) {
            Reservation reservation = next.get();
            reservation.setStatus(ReservationStatus.NOTIFICADO);
            reservationRepository.save(reservation);

            book.setStatus(BookStatus.RESERVADO);
            bookRepository.save(book);

            eventPublisher.publishEvent(new BookAvailableEvent(reservation.getId()));
        } else {
            book.setStatus(BookStatus.DISPONIBLE);
            bookRepository.save(book);
        }
    }
}
