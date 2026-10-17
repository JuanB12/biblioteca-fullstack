package com.juanbarrios.biblioteca.service;

import com.juanbarrios.biblioteca.config.LoanProperties;
import com.juanbarrios.biblioteca.domain.AppUser;
import com.juanbarrios.biblioteca.domain.Book;
import com.juanbarrios.biblioteca.domain.BookStatus;
import com.juanbarrios.biblioteca.domain.Loan;
import com.juanbarrios.biblioteca.domain.Reservation;
import com.juanbarrios.biblioteca.domain.ReservationStatus;
import com.juanbarrios.biblioteca.event.LoanCreatedEvent;
import com.juanbarrios.biblioteca.event.UserBlockedEvent;
import com.juanbarrios.biblioteca.exception.BookNotAvailableException;
import com.juanbarrios.biblioteca.exception.LoanAlreadyReturnedException;
import com.juanbarrios.biblioteca.exception.ResourceNotFoundException;
import com.juanbarrios.biblioteca.exception.UserBlockedException;
import com.juanbarrios.biblioteca.repository.AppUserRepository;
import com.juanbarrios.biblioteca.repository.BookRepository;
import com.juanbarrios.biblioteca.repository.LoanRepository;
import com.juanbarrios.biblioteca.repository.ReservationRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanService.class);

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final AppUserRepository appUserRepository;
    private final ReservationRepository reservationRepository;
    private final BookService bookService;
    private final ReservationService reservationService;
    private final LoanProperties loanProperties;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Loan createLoan(Long bookId, AppUser requester) {
        if (requester.isCurrentlyBlocked()) {
            throw new UserBlockedException(requester.getBlockedUntil());
        }

        Book book = bookService.getByIdOrThrow(bookId);
        Reservation claimedReservation = null;

        switch (book.getStatus()) {
            case PRESTADO -> throw new BookNotAvailableException(
                    "El libro '" + book.getTitle() + "' esta prestado actualmente");
            case RESERVADO -> claimedReservation = claimReservationOrThrow(book, requester.getEmail());
            case DISPONIBLE -> { }
        }

        LocalDate today = LocalDate.now();
        Loan loan = Loan.builder()
                .book(book)
                .borrowerName(requester.getName())
                .borrowerEmail(requester.getEmail())
                .loanDate(today)
                .dueDate(today.plusDays(loanProperties.getDefaultDurationDays()))
                .build();
        loan = loanRepository.save(loan);

        book.setStatus(BookStatus.PRESTADO);
        bookRepository.save(book);

        if (claimedReservation != null) {
            claimedReservation.setStatus(ReservationStatus.CUMPLIDO);
            reservationRepository.save(claimedReservation);
        }

        eventPublisher.publishEvent(new LoanCreatedEvent(loan.getId()));
        return loan;
    }

    private Reservation claimReservationOrThrow(Book book, String requesterEmail) {
        Reservation reservation = reservationRepository
                .findFirstByBookIdAndStatusOrderByRequestDateAsc(book.getId(), ReservationStatus.NOTIFICADO)
                .filter(r -> r.getRequesterEmail().equalsIgnoreCase(requesterEmail))
                .orElseThrow(() -> new BookNotAvailableException(
                        "El libro '" + book.getTitle() + "' esta reservado para otra persona"));
        return reservation;
    }

    @Transactional
    public Loan returnLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un prestamo con id: " + loanId));

        if (loan.isReturned()) {
            throw new LoanAlreadyReturnedException(loanId);
        }

        LocalDate today = LocalDate.now();
        boolean late = today.isAfter(loan.getDueDate());
        loan.setReturnDate(today);
        loanRepository.save(loan);

        reservationService.resolveNextInQueueOrRelease(loan.getBook());

        if (late) {
            applyOverdueConsequencesIfNeeded(loan);
        }

        return loan;
    }

    private void applyOverdueConsequencesIfNeeded(Loan loan) {
        Optional<AppUser> maybeUser = appUserRepository.findByEmailIgnoreCase(loan.getBorrowerEmail());
        if (maybeUser.isEmpty()) {
            log.warn("Prestamo {} devuelto tarde pero no hay cuenta AppUser asociada al correo {}",
                    loan.getId(), loan.getBorrowerEmail());
            return;
        }

        AppUser user = maybeUser.get();
        LocalDate windowStart = LocalDate.now().minusDays(loanProperties.getOverdueWindowDays());
        long lateReturnsInWindow = loanRepository.countLateReturnsSince(user.getEmail(), windowStart);

        if (lateReturnsInWindow >= loanProperties.getMaxOverdueCount() && !user.isCurrentlyBlocked()) {
            user.setBlocked(true);
            user.setBlockedUntil(LocalDateTime.now().plusDays(loanProperties.getBlockDurationDays()));
            appUserRepository.save(user);
            eventPublisher.publishEvent(new UserBlockedEvent(user.getId()));
        }
    }

    public List<Loan> findMine(String borrowerEmail) {
        return loanRepository.findMineWithBook(borrowerEmail);
    }
}
