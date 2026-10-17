package com.juanbarrios.biblioteca.service;

import com.juanbarrios.biblioteca.domain.AppUser;
import com.juanbarrios.biblioteca.domain.BookStatus;
import com.juanbarrios.biblioteca.exception.ResourceNotFoundException;
import com.juanbarrios.biblioteca.repository.AppUserRepository;
import com.juanbarrios.biblioteca.repository.BookRepository;
import com.juanbarrios.biblioteca.repository.LoanRepository;
import com.juanbarrios.biblioteca.web.dto.AdminStatsResponse;
import com.juanbarrios.biblioteca.web.dto.BlockedUserResponse;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final AppUserRepository appUserRepository;

    public AdminStatsResponse getStats() {
        LocalDate today = LocalDate.now();
        return new AdminStatsResponse(
                loanRepository.countActiveLoans(),
                loanRepository.countOverdueLoans(today),
                bookRepository.countByStatus(BookStatus.DISPONIBLE),
                bookRepository.countByStatus(BookStatus.PRESTADO),
                bookRepository.countByStatus(BookStatus.RESERVADO),
                appUserRepository.countByBlockedTrue());
    }

    public List<BlockedUserResponse> getBlockedUsers() {
        return appUserRepository.findByBlockedTrueOrderByBlockedUntilAsc().stream()
                .map(BlockedUserResponse::from)
                .toList();
    }

    @Transactional
    public void unblockUser(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un usuario con id: " + userId));
        user.setBlocked(false);
        user.setBlockedUntil(null);
        appUserRepository.save(user);
    }
}
