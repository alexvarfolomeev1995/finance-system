package com.company.finance.financesystem.service;

import com.company.finance.financesystem.domain.entity.Budget;
import com.company.finance.financesystem.domain.entity.Category;
import com.company.finance.financesystem.domain.entity.Department;
import com.company.finance.financesystem.domain.enums.BudgetPeriod;
import com.company.finance.financesystem.dto.BudgetExecutionResponse;
import com.company.finance.financesystem.dto.BudgetRequest;
import com.company.finance.financesystem.dto.BudgetResponse;
import com.company.finance.financesystem.exception.BusinessException;
import com.company.finance.financesystem.exception.NotFoundException;
import com.company.finance.financesystem.repository.BudgetRepository;
import com.company.finance.financesystem.repository.CategoryRepository;
import com.company.finance.financesystem.repository.DepartmentRepository;
import com.company.finance.financesystem.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final DepartmentRepository departmentRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public List<BudgetResponse> findAll() {
        return budgetRepository.findAll().stream().map(this::toResponse).toList();
    }

    public BudgetResponse findById(Long id) {
        return budgetRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Budget not found: " + id));
    }

    @Transactional
    public BudgetResponse create(BudgetRequest req) {
        if (budgetRepository.findByDepartmentIdAndCategoryIdAndPeriodAndPeriodYearAndPeriodNumber(
                req.departmentId(), req.categoryId(), req.period(),
                req.periodYear(), req.periodNumber()).isPresent()) {
            throw new BusinessException("Budget already exists for this department/category/period");
        }

        Department department = departmentRepository.findById(req.departmentId())
                .orElseThrow(() -> new NotFoundException("Department not found: " + req.departmentId()));
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found: " + req.categoryId()));

        Budget budget = Budget.builder()
                .department(department)
                .category(category)
                .period(req.period())
                .periodYear(req.periodYear())
                .periodNumber(req.periodNumber())
                .plannedAmount(req.plannedAmount())
                .currency(req.currency())
                .build();

        return toResponse(budgetRepository.save(budget));
    }
    @Transactional(readOnly = true)
    public BudgetExecutionResponse getExecution(Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found: " + budgetId));

        LocalDate[] range = periodToDateRange(budget.getPeriod(), budget.getPeriodYear(), budget.getPeriodNumber());

        BigDecimal actual = transactionRepository.sumConfirmedExpenses(
                budget.getDepartment().getId(),
                budget.getCategory().getId(),
                range[0],
                range[1]
        );

        BigDecimal planned = budget.getPlannedAmount();
        BigDecimal remaining = planned.subtract(actual);
        BigDecimal percent = planned.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : actual.multiply(BigDecimal.valueOf(100)).divide(planned, 2, RoundingMode.HALF_UP);

        return new BudgetExecutionResponse(
                budget.getId(),
                budget.getDepartment().getId(),
                budget.getDepartment().getName(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getPeriodYear(),
                budget.getPeriodNumber(),
                planned,
                actual,
                remaining,
                percent
        );
    }

    private LocalDate[] periodToDateRange(BudgetPeriod period, Integer year, Integer number) {
        if (period == BudgetPeriod.MONTH) {
            LocalDate from = LocalDate.of(year, number, 1);
            LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
            return new LocalDate[]{from, to};
        } else { // QUARTER
            int startMonth = (number - 1) * 3 + 1;
            LocalDate from = LocalDate.of(year, startMonth, 1);
            LocalDate to = from.plusMonths(3).minusDays(1);
            return new LocalDate[]{from, to};
        }
    }

    private BudgetResponse toResponse(Budget b) {
        return new BudgetResponse(
                b.getId(),
                b.getDepartment().getId(),
                b.getDepartment().getName(),
                b.getCategory().getId(),
                b.getCategory().getName(),
                b.getPeriod(),
                b.getPeriodYear(),
                b.getPeriodNumber(),
                b.getPlannedAmount(),
                b.getCurrency()
        );
    }
}