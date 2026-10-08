package com.company.finance.financesystem.dto;

import com.company.finance.financesystem.domain.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull TransactionType type,
        @NotNull @Positive BigDecimal amount,
        @NotNull @Size(min = 3, max = 3) String currency,
        @NotNull LocalDate transactionDate,
        @NotNull Long accountId,
        Long targetAccountId,
        Long categoryId,
        Long counterpartyId,
        @NotNull Long departmentId,
        String description
) {}