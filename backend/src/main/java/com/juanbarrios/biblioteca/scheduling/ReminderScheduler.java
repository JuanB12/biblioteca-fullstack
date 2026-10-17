package com.juanbarrios.biblioteca.scheduling;

import com.juanbarrios.biblioteca.config.LoanProperties;
import com.juanbarrios.biblioteca.domain.Loan;
import com.juanbarrios.biblioteca.repository.LoanRepository;
import com.juanbarrios.biblioteca.service.NotificationService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final LoanRepository loanRepository;
    private final LoanProperties loanProperties;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendDueSoonReminders() {
        LocalDate targetDate = LocalDate.now().plusDays(loanProperties.getReminderDaysBeforeDue());
        List<Loan> loans = loanRepository.findDueSoonPendingReminder(targetDate);
        log.info("Job de recordatorios: {} prestamos vencen el {}", loans.size(), targetDate);
        loans.forEach(loan -> notificationService.sendDueSoonReminder(loan.getId()));
    }

    @Scheduled(cron = "0 30 8 * * *")
    public void sendOverdueNotices() {
        List<Loan> loans = loanRepository.findOverduePendingNotice(LocalDate.now());
        log.info("Job de avisos de vencido: {} prestamos vencidos sin notificar", loans.size());
        loans.forEach(loan -> notificationService.sendOverdueNotice(loan.getId()));
    }
}
