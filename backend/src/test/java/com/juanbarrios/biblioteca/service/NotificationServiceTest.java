package com.juanbarrios.biblioteca.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.juanbarrios.biblioteca.domain.Book;
import com.juanbarrios.biblioteca.domain.Loan;
import com.juanbarrios.biblioteca.repository.AppUserRepository;
import com.juanbarrios.biblioteca.repository.LoanRepository;
import com.juanbarrios.biblioteca.repository.ReservationRepository;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);

    @Mock private LoanRepository loanRepository;
    @Mock private AppUserRepository appUserRepository;
    @Mock private ReservationRepository reservationRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() throws Exception {
        greenMail.purgeEmailFromAllMailboxes();

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(greenMail.getSmtp().getPort());

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        notificationService = new NotificationService(
                mailSender, templateEngine, loanRepository, appUserRepository, reservationRepository,
                "biblioteca@biblioteca.local");
    }

    @Test
    void sendLoanConfirmation_sendsRealEmailWithCorrectSubjectRecipientAndBody() throws Exception {
        Book book = Book.builder().id(1L).title("Cien anios de soledad").build();
        Loan loan = Loan.builder().id(10L).book(book).borrowerName("Ana")
                .borrowerEmail("ana@test.com").loanDate(LocalDate.now()).dueDate(LocalDate.now().plusDays(14))
                .build();
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));

        notificationService.sendLoanConfirmation(10L);

        MimeMessage[] received = greenMail.getReceivedMessages();
        assertThat(received).hasSize(1);
        assertThat(received[0].getSubject()).contains("Cien anios de soledad");
        assertThat(received[0].getAllRecipients()[0].toString()).isEqualTo("ana@test.com");
        assertThat(GreenMailUtil.getBody(received[0])).contains("Ana").contains("Cien anios de soledad");
    }

    @Test
    void sendBookAvailable_sendsNothing_whenReservationNoLongerExists() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        notificationService.sendBookAvailable(99L);

        assertThat(greenMail.getReceivedMessages()).isEmpty();
    }
}
