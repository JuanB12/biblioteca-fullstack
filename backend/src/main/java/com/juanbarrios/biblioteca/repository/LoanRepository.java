package com.juanbarrios.biblioteca.repository;

import com.juanbarrios.biblioteca.domain.Loan;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query("SELECT l FROM Loan l JOIN FETCH l.book WHERE LOWER(l.borrowerEmail) = LOWER(:borrowerEmail) ORDER BY l.loanDate DESC")
    List<Loan> findMineWithBook(@Param("borrowerEmail") String borrowerEmail);

    @Query("""
            SELECT l FROM Loan l
            WHERE l.returnDate IS NULL
              AND l.dueDate = :dueDate
              AND l.reminderSentAt IS NULL
            """)
    List<Loan> findDueSoonPendingReminder(@Param("dueDate") LocalDate dueDate);

    @Query("""
            SELECT l FROM Loan l
            WHERE l.returnDate IS NULL
              AND l.dueDate < :today
              AND l.overdueNoticeSentAt IS NULL
            """)
    List<Loan> findOverduePendingNotice(@Param("today") LocalDate today);

    @Query("""
            SELECT COUNT(l) FROM Loan l
            WHERE LOWER(l.borrowerEmail) = LOWER(:borrowerEmail)
              AND l.returnDate IS NOT NULL
              AND l.returnDate > l.dueDate
              AND l.returnDate >= :windowStart
            """)
    long countLateReturnsSince(@Param("borrowerEmail") String borrowerEmail, @Param("windowStart") LocalDate windowStart);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.returnDate IS NULL")
    long countActiveLoans();

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.returnDate IS NULL AND l.dueDate < :today")
    long countOverdueLoans(@Param("today") LocalDate today);
}
