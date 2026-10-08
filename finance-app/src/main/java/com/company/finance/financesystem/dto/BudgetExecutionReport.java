package com.company.finance.financesystem.dto;

import java.time.LocalDate;
import java.util.List;

public record BudgetExecutionReport(
        Long departmentId,
        String departmentName,
        Integer periodYear,
        Integer periodNumber,
        LocalDate from,
        LocalDate to,
        List<BudgetExecutionResponse> budgets
) {}