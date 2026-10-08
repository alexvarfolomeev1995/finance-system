package com.company.finance.financesystem.dto;

import java.math.BigDecimal;

public record BudgetExecutionResponse(
        Long budgetId,
        Long departmentId,
        String departmentName,
        Long categoryId,
        String categoryName,
        Integer periodYear,
        Integer periodNumber,
        BigDecimal plannedAmount,
        BigDecimal actualAmount,
        BigDecimal remainingAmount,
        BigDecimal executionPercent
) {}