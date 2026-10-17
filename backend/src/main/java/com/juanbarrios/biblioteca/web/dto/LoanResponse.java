package com.juanbarrios.biblioteca.web.dto;

import com.juanbarrios.biblioteca.domain.Loan;
import java.time.LocalDate;

public record LoanResponse(
        Long id,
        Long bookId,
        String bookTitle,
        String borrowerName,
        String borrowerEmail,
        LocalDate loanDate,
        LocalDate dueDate,
        LocalDate returnDate,
        boolean overdue) {

    public static LoanResponse from(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getBook().getId(),
                loan.getBook().getTitle(),
                loan.getBorrowerName(),
                loan.getBorrowerEmail(),
                loan.getLoanDate(),
                loan.getDueDate(),
                loan.getReturnDate(),
                !loan.isReturned() && loan.isOverdue());
    }
}
