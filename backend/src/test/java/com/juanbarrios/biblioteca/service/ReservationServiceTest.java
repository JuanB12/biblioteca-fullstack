package com.juanbarrios.biblioteca.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.juanbarrios.biblioteca.domain.Book;
import com.juanbarrios.biblioteca.domain.BookStatus;
import com.juanbarrios.biblioteca.domain.Reservation;
import com.juanbarrios.biblioteca.domain.ReservationStatus;
import com.juanbarrios.biblioteca.domain.Role;
import com.juanbarrios.biblioteca.exception.ReservationNotAllowedException;
import com.juanbarrios.biblioteca.repository.BookRepository;
import com.juanbarrios.biblioteca.repository.ReservationRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private BookRepository bookRepository;
    @Mock private BookService bookService;
    @Mock private ApplicationEventPublisher eventPublisher;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(reservationRepository, bookRepository, bookService, eventPublisher);
    }

    @Test
    void create_throwsReservationNotAllowed_whenBookIsDisponible() {
        Book book = Book.builder().id(1L).status(BookStatus.DISPONIBLE).build();
        when(bookService.getByIdOrThrow(1L)).thenReturn(book);

        assertThatThrownBy(() -> reservationService.create(1L, "ana@test.com"))
                .isInstanceOf(ReservationNotAllowedException.class);
    }

    @Test
    void create_throwsReservationNotAllowed_whenAlreadyHasPendingReservation() {
        Book book = Book.builder().id(1L).status(BookStatus.PRESTADO).build();
        when(bookService.getByIdOrThrow(1L)).thenReturn(book);
        when(reservationRepository.existsByBookIdAndRequesterEmailIgnoreCaseAndStatus(
                1L, "ana@test.com", ReservationStatus.PENDIENTE)).thenReturn(true);

        assertThatThrownBy(() -> reservationService.create(1L, "ana@test.com"))
                .isInstanceOf(ReservationNotAllowedException.class);
    }

    @Test
    void create_succeeds_whenBookIsPrestadoAndNoDuplicateReservation() {
        Book book = Book.builder().id(1L).status(BookStatus.PRESTADO).build();
        when(bookService.getByIdOrThrow(1L)).thenReturn(book);
        when(reservationRepository.existsByBookIdAndRequesterEmailIgnoreCaseAndStatus(
                1L, "ana@test.com", ReservationStatus.PENDIENTE)).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        Reservation reservation = reservationService.create(1L, "ana@test.com");

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDIENTE);
        assertThat(reservation.getRequesterEmail()).isEqualTo("ana@test.com");
    }

    @Test
    void cancel_throwsAccessDenied_whenNotOwnerAndNotAdmin() {
        Reservation reservation = Reservation.builder().id(1L).requesterEmail("ana@test.com")
                .status(ReservationStatus.PENDIENTE).build();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancel(1L, "otro@test.com", Role.BIBLIOTECARIO))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void cancel_allowsAdmin_evenIfNotOwner() {
        Reservation reservation = Reservation.builder().id(1L).requesterEmail("ana@test.com")
                .status(ReservationStatus.PENDIENTE).build();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        reservationService.cancel(1L, "admin@test.com", Role.ADMIN);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELADO);
    }

    @Test
    void cancel_throwsReservationNotAllowed_whenAlreadyCumplido() {
        Reservation reservation = Reservation.builder().id(1L).requesterEmail("ana@test.com")
                .status(ReservationStatus.CUMPLIDO).build();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancel(1L, "ana@test.com", Role.BIBLIOTECARIO))
                .isInstanceOf(ReservationNotAllowedException.class);
    }

    @Test
    void cancel_releasesBookToNextInQueue_whenCancellingNotificado() {
        Book book = Book.builder().id(1L).status(BookStatus.RESERVADO).build();
        Reservation cancelled = Reservation.builder().id(1L).book(book).requesterEmail("ana@test.com")
                .status(ReservationStatus.NOTIFICADO).build();
        Reservation nextInLine = Reservation.builder().id(2L).requesterEmail("luis@test.com")
                .status(ReservationStatus.PENDIENTE).build();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(cancelled));
        when(reservationRepository.findFirstByBookIdAndStatusOrderByRequestDateAsc(1L, ReservationStatus.PENDIENTE))
                .thenReturn(Optional.of(nextInLine));

        reservationService.cancel(1L, "ana@test.com", Role.BIBLIOTECARIO);

        assertThat(cancelled.getStatus()).isEqualTo(ReservationStatus.CANCELADO);
        assertThat(nextInLine.getStatus()).isEqualTo(ReservationStatus.NOTIFICADO);
        assertThat(book.getStatus()).isEqualTo(BookStatus.RESERVADO);
    }

    @Test
    void cancel_doesNotTouchBook_whenCancellingPendiente() {
        Reservation reservation = Reservation.builder().id(1L).requesterEmail("ana@test.com")
                .status(ReservationStatus.PENDIENTE).build();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        reservationService.cancel(1L, "ana@test.com", Role.BIBLIOTECARIO);

        verify(bookRepository, never()).save(any());
    }

    @Test
    void resolveNextInQueueOrRelease_setsDisponible_whenNoPendingReservations() {
        Book book = Book.builder().id(1L).status(BookStatus.PRESTADO).build();
        when(reservationRepository.findFirstByBookIdAndStatusOrderByRequestDateAsc(1L, ReservationStatus.PENDIENTE))
                .thenReturn(Optional.empty());

        reservationService.resolveNextInQueueOrRelease(book);

        assertThat(book.getStatus()).isEqualTo(BookStatus.DISPONIBLE);
    }

    @Test
    void resolveNextInQueueOrRelease_notifiesNext_whenPendingReservationExists() {
        Book book = Book.builder().id(1L).status(BookStatus.PRESTADO).build();
        Reservation next = Reservation.builder().id(2L).requesterEmail("luis@test.com")
                .status(ReservationStatus.PENDIENTE).build();
        when(reservationRepository.findFirstByBookIdAndStatusOrderByRequestDateAsc(1L, ReservationStatus.PENDIENTE))
                .thenReturn(Optional.of(next));

        reservationService.resolveNextInQueueOrRelease(book);

        assertThat(book.getStatus()).isEqualTo(BookStatus.RESERVADO);
        assertThat(next.getStatus()).isEqualTo(ReservationStatus.NOTIFICADO);
    }
}
