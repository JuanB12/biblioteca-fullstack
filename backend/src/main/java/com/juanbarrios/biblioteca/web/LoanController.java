package com.juanbarrios.biblioteca.web;

import com.juanbarrios.biblioteca.domain.Loan;
import com.juanbarrios.biblioteca.security.AppUserPrincipal;
import com.juanbarrios.biblioteca.service.LoanService;
import com.juanbarrios.biblioteca.web.dto.LoanCreateRequest;
import com.juanbarrios.biblioteca.web.dto.LoanResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<LoanResponse> create(
            @Valid @RequestBody LoanCreateRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal) {
        Loan loan = loanService.createLoan(request.bookId(), principal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(LoanResponse.from(loan));
    }

    @GetMapping("/mine")
    public List<LoanResponse> mine(@AuthenticationPrincipal AppUserPrincipal principal) {
        return loanService.findMine(principal.getUser().getEmail()).stream()
                .map(LoanResponse::from)
                .toList();
    }

    @PutMapping("/{id}/return")
    public LoanResponse returnLoan(@PathVariable Long id) {
        return LoanResponse.from(loanService.returnLoan(id));
    }
}
