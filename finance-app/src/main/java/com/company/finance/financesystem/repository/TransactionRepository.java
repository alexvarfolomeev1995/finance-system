package com.company.finance.financesystem.repository;

import com.company.finance.financesystem.domain.entity.Transaction;
import com.company.finance.financesystem.domain.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findAllByDepartmentId(Long departmentId, Pageable pageable);

    List<Transaction> findAllByAccountIdAndStatus(Long accountId, TransactionStatus status);

    List<Transaction> findAllByReversalOfId(Long reversalOfId);


    @Query("""
    SELECT COALESCE(SUM(
        CASE
            WHEN t.type = 'INCOME'   AND t.account.id = :accountId THEN t.amount
            WHEN t.type = 'EXPENSE'  AND t.account.id = :accountId THEN -t.amount
            WHEN t.type = 'TRANSFER' AND t.account.id = :accountId THEN -t.amount
            WHEN t.type = 'TRANSFER' AND t.targetAccount.id = :accountId THEN t.amount
            ELSE 0
        END
    ), 0)
    FROM Transaction t
    WHERE t.status = 'CONFIRMED'
      AND t.reversalOf IS NULL
      AND (t.account.id = :accountId OR t.targetAccount.id = :accountId)
""")
    java.math.BigDecimal calculateBalanceDelta(@Param("accountId") Long accountId);



    @Query("""
    SELECT c.id, c.name, t.type, COALESCE(SUM(t.amount), 0)
    FROM Transaction t
    JOIN t.category c
    WHERE t.department.id = :departmentId
      AND t.status = 'CONFIRMED'
      AND t.reversalOf IS NULL
      AND t.transactionDate BETWEEN :from AND :to
    GROUP BY c.id, c.name, t.type
    ORDER BY c.name
""")
    List<Object[]> aggregateByCategory(
            @Param("departmentId") Long departmentId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );


    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM Transaction t
    WHERE t.department.id = :departmentId
      AND t.category.id = :categoryId
      AND t.type = 'EXPENSE'
      AND t.status = 'CONFIRMED'
      AND t.reversalOf IS NULL
      AND t.transactionDate BETWEEN :from AND :to
""")
    java.math.BigDecimal sumConfirmedExpenses(
            @Param("departmentId") Long departmentId,
            @Param("categoryId") Long categoryId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}