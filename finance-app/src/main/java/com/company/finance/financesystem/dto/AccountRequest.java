package com.company.finance.financesystem.dto;

import com.company.finance.financesystem.domain.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AccountRequest(
        @NotNull Long departmentId,
        @NotBlank String name,
        @NotNull AccountType type,
        @NotBlank @Size(min = 3, max = 3) String currency,
        BigDecimal initialBalance
) {}