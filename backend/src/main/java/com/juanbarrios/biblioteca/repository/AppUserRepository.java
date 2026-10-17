package com.juanbarrios.biblioteca.repository;

import com.juanbarrios.biblioteca.domain.AppUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByBlockedTrue();

    List<AppUser> findByBlockedTrueOrderByBlockedUntilAsc();
}
