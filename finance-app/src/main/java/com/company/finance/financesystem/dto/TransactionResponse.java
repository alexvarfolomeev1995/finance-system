package com.company.finance.financesystem.dto;

import com.company.finance.financesystem.domain.enums.TransactionStatus;
import com.company.finance.financesystem.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        TransactionType type,
        TransactionStatus status,
        BigDecimal amount,
        String currency,
        LocalDate transactionDate,
        Long accountId,
        Long targetAccountId,
        Long categoryId,
        Long counterpartyId,
        Long departmentId,
        Long authorId,
        Long reversalOfId,
        String description,
        Instant createdAt
) {}