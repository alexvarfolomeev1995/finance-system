package com.company.finance.financesystem.service;

import com.company.finance.financesystem.domain.entity.Budget;
import com.company.finance.financesystem.domain.entity.Department;
import com.company.finance.financesystem.domain.enums.BudgetPeriod;
import com.company.finance.financesystem.domain.enums.TransactionType;
import com.company.finance.financesystem.dto.*;
import com.company.finance.financesystem.exception.NotFoundException;
import com.company.finance.financesystem.repository.BudgetRepository;
import com.company.finance.financesystem.repository.DepartmentRepository;
import com.company.finance.financesystem.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final DepartmentRepository departmentRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetService budgetService;

    @Transactional(readOnly = true)
    public CashFlowReport getCashFlow(Long departmentId, LocalDate from, LocalDate to) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found: " + departmentId));

        List<Object[]> rows = transactionRepository.aggregateByCategory(departmentId, from, to);

        Map<Long, String> categoryNames = new LinkedHashMap<>();
        Map<Long, BigDecimal> incomeMap = new LinkedHashMap<>();
        Map<Long, BigDecimal> expenseMap = new LinkedHashMap<>();

        for (Object[] row : rows) {
            Long categoryId = (Long) row[0];
            String categoryName = (String) row[1];
            TransactionType type = (TransactionType) row[2];
            BigDecimal sum = (BigDecimal) row[3];

            categoryNames.putIfAbsent(categoryId, categoryName);
            if (type == TransactionType.INCOME) {
                incomeMap.merge(categoryId, sum, BigDecimal::add);
            } else if (type == TransactionType.EXPENSE) {
                expenseMap.merge(categoryId, sum, BigDecimal::add);
            }
        }

        List<CashFlowItem> items = new ArrayList<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Long categoryId : categoryNames.keySet()) {
            BigDecimal income = incomeMap.getOrDefault(categoryId, BigDecimal.ZERO);
            BigDecimal expense = expenseMap.getOrDefault(categoryId, BigDecimal.ZERO);
            BigDecimal net = income.subtract(expense);

            items.add(new CashFlowItem(categoryId, categoryNames.get(categoryId), income, expense, net));
            totalIncome = totalIncome.add(income);
            totalExpense = totalExpense.add(expense);
        }

        return new CashFlowReport(
                department.getId(),
                department.getName(),
                from,
                to,
                totalIncome,
                totalExpense,
                totalIncome.subtract(totalExpense),
                items
        );
    }

    @Transactional(readOnly = true)
    public BudgetExecutionReport getBudgetExecutionReport(
            Long departmentId, BudgetPeriod period, Integer year, Integer number
    ) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found: " + departmentId));

        LocalDate[] range = periodToRange(period, year, number);

        List<Budget> budgets = budgetRepository
                .findAllByDepartmentIdAndPeriodYearAndPeriodNumber(departmentId, year, number);

        List<BudgetExecutionResponse> responses = budgets.stream()
                .map(b -> budgetService.getExecution(b.getId()))
                .toList();

        return new BudgetExecutionReport(
                department.getId(),
                department.getName(),
                year,
                number,
                range[0],
                range[1],
                responses
        );
    }

    private LocalDate[] periodToRange(BudgetPeriod period, Integer year, Integer number) {
        if (period == BudgetPeriod.MONTH) {
            LocalDate from = LocalDate.of(year, number, 1);
            return new LocalDate[]{from, from.withDayOfMonth(from.lengthOfMonth())};
        } else {
            int startMonth = (number - 1) * 3 + 1;
            LocalDate from = LocalDate.of(year, startMonth, 1);
            return new LocalDate[]{from, from.plusMonths(3).minusDays(1)};
        }
    }
}