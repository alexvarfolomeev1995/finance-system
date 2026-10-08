package com.company.finance.financesystem.controller;

import com.company.finance.financesystem.dto.CounterpartyRequest;
import com.company.finance.financesystem.dto.CounterpartyResponse;
import com.company.finance.financesystem.service.CounterpartyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/counterparties")
@RequiredArgsConstructor
public class CounterpartyController {

    private final CounterpartyService counterpartyService;

    @GetMapping
    public List<CounterpartyResponse> findAll() {
        return counterpartyService.findAll();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER')")
    public CounterpartyResponse create(@Valid @RequestBody CounterpartyRequest req) {
        return counterpartyService.create(req);
    }
}