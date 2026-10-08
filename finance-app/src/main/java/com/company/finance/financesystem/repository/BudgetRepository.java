package com.company.finance.financesystem.repository;

import com.company.finance.financesystem.domain.entity.Budget;
import com.company.finance.financesystem.domain.enums.BudgetPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findAllByDepartmentId(Long departmentId);

    List<Budget> findAllByDepartmentIdAndPeriodYearAndPeriodNumber(
            Long departmentId, Integer periodYear, Integer periodNumber);

    Optional<Budget> findByDepartmentIdAndCategoryIdAndPeriodAndPeriodYearAndPeriodNumber(
            Long departmentId,
            Long categoryId,
            BudgetPeriod period,
            Integer periodYear,
            Integer periodNumber
    );
}