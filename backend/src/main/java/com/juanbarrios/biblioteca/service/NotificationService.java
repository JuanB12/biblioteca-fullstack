package com.juanbarrios.biblioteca.service;

import com.juanbarrios.biblioteca.domain.AppUser;
import com.juanbarrios.biblioteca.domain.Loan;
import com.juanbarrios.biblioteca.domain.Reservation;
import com.juanbarrios.biblioteca.repository.AppUserRepository;
import com.juanbarrios.biblioteca.repository.LoanRepository;
import com.juanbarrios.biblioteca.repository.ReservationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final LoanRepository loanRepository;
    private final AppUserRepository appUserRepository;
    private final ReservationRepository reservationRepository;
    private final String fromAddress;

    public NotificationService(
            JavaMailSender mailSender,
            TemplateEngine templateEngine,
            LoanRepository loanRepository,
            AppUserRepository appUserRepository,
            ReservationRepository reservationRepository,
            @Value("${app.mail.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.loanRepository = loanRepository;
        this.appUserRepository = appUserRepository;
        this.reservationRepository = reservationRepository;
        this.fromAddress = fromAddress;
    }

    @Transactional(readOnly = true)
    public void sendLoanConfirmation(Long loanId) {
        loanRepository.findById(loanId).ifPresentOrElse(loan -> {
            Context ctx = new Context();
            ctx.setVariable("borrowerName", loan.getBorrowerName());
            ctx.setVariable("bookTitle", loan.getBook().getTitle());
            ctx.setVariable("loanDate", loan.getLoanDate().format(DATE_FMT));
            ctx.setVariable("dueDate", loan.getDueDate().format(DATE_FMT));

            send(loan.getBorrowerEmail(), "Confirmacion de prestamo: " + loan.getBook().getTitle(),
                    "loan-confirmation", ctx);
        }, () -> log.warn("sendLoanConfirmation: prestamo {} ya no existe", loanId));
    }

    @Transactional
    public void sendDueSoonReminder(Long loanId) {
        loanRepository.findById(loanId).ifPresentOrElse(loan -> {
            Context ctx = new Context();
            ctx.setVariable("borrowerName", loan.getBorrowerName());
            ctx.setVariable("bookTitle", loan.getBook().getTitle());
            ctx.setVariable("dueDate", loan.getDueDate().format(DATE_FMT));

            boolean sent = send(loan.getBorrowerEmail(), "Tu prestamo vence pronto: " + loan.getBook().getTitle(),
                    "due-soon-reminder", ctx);
            if (sent) {
                loan.setReminderSentAt(LocalDateTime.now());
                loanRepository.save(loan);
            }
        }, () -> log.warn("sendDueSoonReminder: prestamo {} ya no existe", loanId));
    }

    @Transactional
    public void sendOverdueNotice(Long loanId) {
        loanRepository.findById(loanId).ifPresentOrElse(loan -> {
            long daysOverdue = ChronoUnit.DAYS.between(loan.getDueDate(), java.time.LocalDate.now());
            Context ctx = new Context();
            ctx.setVariable("borrowerName", loan.getBorrowerName());
            ctx.setVariable("bookTitle", loan.getBook().getTitle());
            ctx.setVariable("dueDate", loan.getDueDate().format(DATE_FMT));
            ctx.setVariable("daysOverdue", daysOverdue);

            boolean sent = send(loan.getBorrowerEmail(), "Prestamo vencido: " + loan.getBook().getTitle(),
                    "overdue-notice", ctx);
            if (sent) {
                loan.setOverdueNoticeSentAt(LocalDateTime.now());
                loanRepository.save(loan);
            }
        }, () -> log.warn("sendOverdueNotice: prestamo {} ya no existe", loanId));
    }

    public void sendBlockedNotice(Long userId) {
        appUserRepository.findById(userId).ifPresentOrElse(this::sendBlockedNotice,
                () -> log.warn("sendBlockedNotice: usuario {} ya no existe", userId));
    }

    private void sendBlockedNotice(AppUser user) {
        Context ctx = new Context();
        ctx.setVariable("userName", user.getName());
        ctx.setVariable("blockedUntil", user.getBlockedUntil() != null
                ? user.getBlockedUntil().format(DATE_TIME_FMT)
                : "que un administrador levante el bloqueo");

        send(user.getEmail(), "Tu cuenta fue bloqueada temporalmente", "account-blocked", ctx);
    }

    @Transactional(readOnly = true)
    public void sendBookAvailable(Long reservationId) {
        reservationRepository.findById(reservationId).ifPresentOrElse(reservation -> {
            Context ctx = new Context();
            ctx.setVariable("bookTitle", reservation.getBook().getTitle());

            send(reservation.getRequesterEmail(), "Ya puedes recoger: " + reservation.getBook().getTitle(),
                    "book-available", ctx);
        }, () -> log.warn("sendBookAvailable: reserva {} ya no existe", reservationId));
    }

    private boolean send(String to, String subject, String templateName, Context context) {
        try {
            String html = templateEngine.process("email/" + templateName, context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setFrom(fromAddress);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            return true;
        } catch (MessagingException | RuntimeException e) {
            log.error("No se pudo enviar el correo '{}' a {}", subject, to, e);
            return false;
        }
    }
}
