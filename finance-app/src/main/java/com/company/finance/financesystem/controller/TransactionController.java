package com.company.finance.financesystem.controller;

import com.company.finance.financesystem.dto.TransactionRequest;
import com.company.finance.financesystem.dto.TransactionResponse;
import com.company.finance.financesystem.dto.TransferRequest;
import com.company.finance.financesystem.security.CustomUserDetailsService;
import com.company.finance.financesystem.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final CustomUserDetailsService userDetailsService;

    private final com.company.finance.financesystem.repository.UserRepository userRepository;

    private Long currentUserId(Authentication auth) {
        UserDetails ud = (UserDetails) auth.getPrincipal();
        return userRepository.findByEmail(ud.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"))
                .getId();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER', 'ACCOUNTANT', 'AUDITOR')")
    public Page<TransactionResponse> list(
            @RequestParam Long departmentId,
            Pageable pageable
    ) {
        return transactionService.findAllByDepartment(departmentId, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER', 'ACCOUNTANT', 'AUDITOR')")
    public TransactionResponse findById(@PathVariable Long id) {
        return transactionService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody TransactionRequest req,
            Authentication auth
    ) {
        TransactionResponse response = transactionService.create(req, currentUserId(auth));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public TransactionResponse confirm(@PathVariable Long id) {
        return transactionService.confirm(id);
    }

    @PostMapping("/{id}/reverse")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public TransactionResponse reverse(@PathVariable Long id, Authentication auth) {
        return transactionService.reverse(id, currentUserId(auth));
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest req,
            Authentication auth
    ) {
        TransactionResponse response = transactionService.transfer(req, currentUserId(auth));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<Void> deleteDraft(@PathVariable Long id) {
        transactionService.deleteDraft(id);
        return ResponseEntity.noContent().build();
    }
}