package com.company.finance.financesystem.dto;

import com.company.finance.financesystem.domain.enums.BudgetPeriod;

import java.math.BigDecimal;

public record BudgetResponse(
        Long id,
        Long departmentId,
        String departmentName,
        Long categoryId,
        String categoryName,
        BudgetPeriod period,
        Integer periodYear,
        Integer periodNumber,
        BigDecimal plannedAmount,
        String currency
) {}