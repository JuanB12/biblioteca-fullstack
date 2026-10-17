package com.juanbarrios.biblioteca.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.juanbarrios.biblioteca.domain.AppUser;
import com.juanbarrios.biblioteca.domain.Role;
import com.juanbarrios.biblioteca.exception.ResourceNotFoundException;
import com.juanbarrios.biblioteca.repository.AppUserRepository;
import com.juanbarrios.biblioteca.repository.BookRepository;
import com.juanbarrios.biblioteca.repository.LoanRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private LoanRepository loanRepository;
    @Mock private BookRepository bookRepository;
    @Mock private AppUserRepository appUserRepository;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(loanRepository, bookRepository, appUserRepository);
    }

    @Test
    void unblockUser_clearsBlockedFlagAndDate() {
        AppUser user = AppUser.builder().id(1L).name("Juan").email("juan@test.com").role(Role.BIBLIOTECARIO)
                .blocked(true).blockedUntil(LocalDateTime.now().plusDays(5)).build();
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user));

        adminService.unblockUser(1L);

        assertThat(user.isBlocked()).isFalse();
        assertThat(user.getBlockedUntil()).isNull();
    }

    @Test
    void unblockUser_throwsResourceNotFound_whenUserMissing() {
        when(appUserRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.unblockUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
