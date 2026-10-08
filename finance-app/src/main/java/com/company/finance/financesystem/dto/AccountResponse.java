package com.company.finance.financesystem.dto;

import com.company.finance.financesystem.domain.enums.AccountType;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        Long departmentId,
        String departmentName,
        String name,
        AccountType type,
        String currency,
        BigDecimal initialBalance,
        BigDecimal currentBalance
) {}