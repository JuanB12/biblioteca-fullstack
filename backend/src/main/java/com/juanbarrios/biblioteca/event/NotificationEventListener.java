package com.juanbarrios.biblioteca.event;

import com.juanbarrios.biblioteca.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLoanCreated(LoanCreatedEvent event) {
        notificationService.sendLoanConfirmation(event.loanId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserBlocked(UserBlockedEvent event) {
        notificationService.sendBlockedNotice(event.userId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookAvailable(BookAvailableEvent event) {
        notificationService.sendBookAvailable(event.reservationId());
    }
}
