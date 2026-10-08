package com.company.finance.financesystem.dto;

import com.company.finance.financesystem.domain.enums.BudgetPeriod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record BudgetRequest(
        @NotNull Long departmentId,
        @NotNull Long categoryId,
        @NotNull BudgetPeriod period,
        @NotNull Integer periodYear,
        @NotNull Integer periodNumber,
        @NotNull @Positive BigDecimal plannedAmount,
        @NotNull @Size(min = 3, max = 3) String currency
) {}