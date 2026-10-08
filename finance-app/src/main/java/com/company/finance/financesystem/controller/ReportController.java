package com.company.finance.financesystem.controller;

import com.company.finance.financesystem.domain.enums.BudgetPeriod;
import com.company.finance.financesystem.dto.BudgetExecutionReport;
import com.company.finance.financesystem.dto.CashFlowReport;
import com.company.finance.financesystem.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;


    @GetMapping("/cash-flow")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER', 'ACCOUNTANT', 'AUDITOR')")
    public CashFlowReport cashFlow(
            @RequestParam Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return reportService.getCashFlow(departmentId, from, to);
    }


    @GetMapping("/budget-execution")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_MANAGER', 'ACCOUNTANT', 'AUDITOR')")
    public BudgetExecutionReport budgetExecution(
            @RequestParam Long departmentId,
            @RequestParam BudgetPeriod period,
            @RequestParam Integer year,
            @RequestParam Integer number
    ) {
        return reportService.getBudgetExecutionReport(departmentId, period, year, number);
    }
}