package com.company.finance.financesystem.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CashFlowReport(
        Long departmentId,
        String departmentName,
        LocalDate from,
        LocalDate to,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netCashFlow,
        List<CashFlowItem> items
) {}