package com.company.finance.financesystem.service;

import com.company.finance.financesystem.domain.entity.Budget;
import com.company.finance.financesystem.domain.entity.Category;
import com.company.finance.financesystem.domain.entity.Department;
import com.company.finance.financesystem.domain.enums.BudgetPeriod;
import com.company.finance.financesystem.repository.BudgetRepository;
import com.company.finance.financesystem.repository.CategoryRepository;
import com.company.finance.financesystem.repository.DepartmentRepository;
import com.company.finance.financesystem.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock private BudgetRepository budgetRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private TransactionRepository transactionRepository;

    @InjectMocks
    private BudgetService budgetService;

    @Test
    void getExecution_shouldCalculatePercentCorrectly() {
        Department dept = Department.builder().id(1L).name("IT").build();
        Category cat = Category.builder().id(1L).name("Salary").build();
        Budget budget = Budget.builder()
                .id(1L)
                .department(dept)
                .category(cat)
                .period(BudgetPeriod.MONTH)
                .periodYear(2026)
                .periodNumber(10)
                .plannedAmount(BigDecimal.valueOf(50000))
                .currency("RUB")
                .build();

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));
        when(transactionRepository.sumConfirmedExpenses(eq(1L), eq(1L), any(), any()))
                .thenReturn(BigDecimal.valueOf(5000));

        var response = budgetService.getExecution(1L);

        assertThat(response.plannedAmount()).isEqualByComparingTo("50000");
        assertThat(response.actualAmount()).isEqualByComparingTo("5000");
        assertThat(response.remainingAmount()).isEqualByComparingTo("45000");
        assertThat(response.executionPercent()).isEqualByComparingTo("10.00");
    }

    @Test
    void getExecution_shouldReturnZeroPercent_whenPlannedAmountIsZero() {
        Department dept = Department.builder().id(1L).name("IT").build();
        Category cat = Category.builder().id(1L).name("Salary").build();
        Budget budget = Budget.builder()
                .id(1L)
                .department(dept)
                .category(cat)
                .period(BudgetPeriod.MONTH)
                .periodYear(2026)
                .periodNumber(10)
                .plannedAmount(BigDecimal.ZERO)
                .currency("RUB")
                .build();

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));
        when(transactionRepository.sumConfirmedExpenses(eq(1L), eq(1L), any(), any()))
                .thenReturn(BigDecimal.valueOf(100));

        var response = budgetService.getExecution(1L);

        assertThat(response.executionPercent()).isEqualByComparingTo("0");
    }
}