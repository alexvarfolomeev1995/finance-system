package com.company.finance.financesystem.controller;

import com.company.finance.financesystem.dto.AccountRequest;
import com.company.finance.financesystem.dto.AccountResponse;
import com.company.finance.financesystem.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public List<AccountResponse> findAll() {
        return accountService.findAll();
    }

    @GetMapping("/{id}")
    public AccountResponse findById(@PathVariable Long id) {
        return accountService.findById(id);
    }

    @GetMapping("/{id}/balance")
    public BigDecimal balance(@PathVariable Long id) {
        return accountService.calculateBalance(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    public AccountResponse create(@Valid @RequestBody AccountRequest req) {
        return accountService.create(req);
    }
}