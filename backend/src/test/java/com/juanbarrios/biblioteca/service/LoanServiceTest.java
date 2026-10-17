package com.juanbarrios.biblioteca.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.juanbarrios.biblioteca.config.LoanProperties;
import com.juanbarrios.biblioteca.domain.AppUser;
import com.juanbarrios.biblioteca.domain.Book;
import com.juanbarrios.biblioteca.domain.BookStatus;
import com.juanbarrios.biblioteca.domain.Loan;
import com.juanbarrios.biblioteca.domain.Reservation;
import com.juanbarrios.biblioteca.domain.ReservationStatus;
import com.juanbarrios.biblioteca.domain.Role;
import com.juanbarrios.biblioteca.event.LoanCreatedEvent;
import com.juanbarrios.biblioteca.event.UserBlockedEvent;
import com.juanbarrios.biblioteca.exception.BookNotAvailableException;
import com.juanbarrios.biblioteca.exception.LoanAlreadyReturnedException;
import com.juanbarrios.biblioteca.exception.UserBlockedException;
import com.juanbarrios.biblioteca.repository.AppUserRepository;
import com.juanbarrios.biblioteca.repository.BookRepository;
import com.juanbarrios.biblioteca.repository.LoanRepository;
import com.juanbarrios.biblioteca.repository.ReservationRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock private LoanRepository loanRepository;
    @Mock private BookRepository bookRepository;
    @Mock private AppUserRepository appUserRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private BookService bookService;
    @Mock private ReservationService reservationService;
    @Mock private ApplicationEventPublisher eventPublisher;

    private LoanService loanService;

    @BeforeEach
    void setUp() {
        loanService = new LoanService(
                loanRepository, bookRepository, appUserRepository, reservationRepository,
                bookService, reservationService, new LoanProperties(), eventPublisher);
    }

    private AppUser borrower() {
        return AppUser.builder()
                .id(1L).name("Juan").email("juan@test.com").role(Role.BIBLIOTECARIO).blocked(false)
                .build();
    }

    @Test
    void createLoan_throwsUserBlocked_whenRequesterIsBlocked() {
        AppUser blocked = borrower();
        blocked.setBlocked(true);
        blocked.setBlockedUntil(LocalDateTime.now().plusDays(3));

        assertThatThrownBy(() -> loanService.createLoan(10L, blocked))
                .isInstanceOf(UserBlockedException.class);

        verify(bookService, never()).getByIdOrThrow(any());
    }

    @Test
    void createLoan_throwsBookNotAvailable_whenBookIsPrestado() {
        Book book = Book.builder().id(10L).title("Libro").status(BookStatus.PRESTADO).build();
        when(bookService.getByIdOrThrow(10L)).thenReturn(book);

        assertThatThrownBy(() -> loanService.createLoan(10L, borrower()))
                .isInstanceOf(BookNotAvailableException.class);

        verify(loanRepository, never()).save(any());
    }

    @Test
    void createLoan_throwsBookNotAvailable_whenReservadoForSomeoneElse() {
        Book book = Book.builder().id(10L).title("Libro").status(BookStatus.RESERVADO).build();
        when(bookService.getByIdOrThrow(10L)).thenReturn(book);
        when(reservationRepository.findFirstByBookIdAndStatusOrderByRequestDateAsc(10L, ReservationStatus.NOTIFICADO))
                .thenReturn(Optional.of(Reservation.builder().requesterEmail("otro@test.com").build()));

        assertThatThrownBy(() -> loanService.createLoan(10L, borrower()))
                .isInstanceOf(BookNotAvailableException.class);
    }

    @Test
    void createLoan_succeeds_whenReservadoForRequester_marksReservationCumplido() {
        Book book = Book.builder().id(10L).title("Libro").status(BookStatus.RESERVADO).build();
        Reservation reservation = Reservation.builder().id(5L).requesterEmail("juan@test.com")
                .status(ReservationStatus.NOTIFICADO).build();
        when(bookService.getByIdOrThrow(10L)).thenReturn(book);
        when(reservationRepository.findFirstByBookIdAndStatusOrderByRequestDateAsc(10L, ReservationStatus.NOTIFICADO))
                .thenReturn(Optional.of(reservation));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        loanService.createLoan(10L, borrower());

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CUMPLIDO);
        assertThat(book.getStatus()).isEqualTo(BookStatus.PRESTADO);
        verify(reservationRepository).save(reservation);
    }

    @Test
    void createLoan_succeeds_whenDisponible_setsDueDateAndPublishesEvent() {
        Book book = Book.builder().id(10L).title("Libro").status(BookStatus.DISPONIBLE).build();
        when(bookService.getByIdOrThrow(10L)).thenReturn(book);
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> {
            Loan loan = inv.getArgument(0);
            loan.setId(99L);
            return loan;
        });

        Loan loan = loanService.createLoan(10L, borrower());

        assertThat(loan.getDueDate()).isEqualTo(loan.getLoanDate().plusDays(14));
        assertThat(book.getStatus()).isEqualTo(BookStatus.PRESTADO);
        verify(eventPublisher).publishEvent(new LoanCreatedEvent(99L));
    }

    @Test
    void returnLoan_throwsLoanAlreadyReturned_whenAlreadyReturned() {
        Loan loan = Loan.builder().id(1L).returnDate(LocalDate.now().minusDays(1)).build();
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanService.returnLoan(1L))
                .isInstanceOf(LoanAlreadyReturnedException.class);
    }

    @Test
    void returnLoan_onTime_doesNotBlockUser() {
        Book book = Book.builder().id(10L).status(BookStatus.PRESTADO).build();
        Loan loan = Loan.builder().id(1L).book(book).borrowerEmail("juan@test.com")
                .loanDate(LocalDate.now().minusDays(5)).dueDate(LocalDate.now().plusDays(1)).build();
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        loanService.returnLoan(1L);

        verify(appUserRepository, never()).findByEmailIgnoreCase(anyString());
        verify(reservationService).resolveNextInQueueOrRelease(book);
    }

    @Test
    void returnLoan_late_blocksUser_whenReachesThirdLateReturnInWindow() {
        Book book = Book.builder().id(10L).status(BookStatus.PRESTADO).build();
        Loan loan = Loan.builder().id(1L).book(book).borrowerEmail("juan@test.com")
                .loanDate(LocalDate.now().minusDays(20)).dueDate(LocalDate.now().minusDays(1)).build();
        AppUser user = borrower();
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(appUserRepository.findByEmailIgnoreCase("juan@test.com")).thenReturn(Optional.of(user));
        when(loanRepository.countLateReturnsSince(eq("juan@test.com"), any())).thenReturn(3L);

        loanService.returnLoan(1L);

        assertThat(user.isBlocked()).isTrue();
        assertThat(user.getBlockedUntil()).isAfter(LocalDateTime.now().plusDays(6));
        verify(appUserRepository).save(user);
        verify(eventPublisher, times(1)).publishEvent(any(UserBlockedEvent.class));
    }

    @Test
    void returnLoan_late_doesNotBlock_whenBelowThreshold() {
        Book book = Book.builder().id(10L).status(BookStatus.PRESTADO).build();
        Loan loan = Loan.builder().id(1L).book(book).borrowerEmail("juan@test.com")
                .loanDate(LocalDate.now().minusDays(20)).dueDate(LocalDate.now().minusDays(1)).build();
        AppUser user = borrower();
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(appUserRepository.findByEmailIgnoreCase("juan@test.com")).thenReturn(Optional.of(user));
        when(loanRepository.countLateReturnsSince(eq("juan@test.com"), any())).thenReturn(1L);

        loanService.returnLoan(1L);

        assertThat(user.isBlocked()).isFalse();
        verify(appUserRepository, never()).save(any());
    }
}
