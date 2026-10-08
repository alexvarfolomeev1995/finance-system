package com.company.finance.financesystem.service;

import com.company.finance.financesystem.domain.entity.Account;
import com.company.finance.financesystem.domain.entity.Department;
import com.company.finance.financesystem.dto.AccountRequest;
import com.company.finance.financesystem.dto.AccountResponse;
import com.company.finance.financesystem.exception.NotFoundException;
import com.company.finance.financesystem.repository.AccountRepository;
import com.company.finance.financesystem.repository.DepartmentRepository;
import com.company.finance.financesystem.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final DepartmentRepository departmentRepository;
    private final TransactionRepository transactionRepository;

    public List<AccountResponse> findAll() {
        return accountRepository.findAll().stream().map(this::toResponse).toList();
    }

    public AccountResponse findById(Long id) {
        return accountRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Account not found: " + id));
    }

    public BigDecimal calculateBalance(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));
        BigDecimal delta = transactionRepository.calculateBalanceDelta(accountId);
        return account.getInitialBalance().add(delta);
    }

    @Transactional
    public AccountResponse create(AccountRequest req) {
        Department d = departmentRepository.findById(req.departmentId())
                .orElseThrow(() -> new NotFoundException("Department not found: " + req.departmentId()));
        Account a = Account.builder()
                .department(d)
                .name(req.name())
                .type(req.type())
                .currency(req.currency())
                .initialBalance(req.initialBalance() != null ? req.initialBalance() : BigDecimal.ZERO)
                .build();
        return toResponse(accountRepository.save(a));
    }

    private AccountResponse toResponse(Account a) {
        BigDecimal delta = transactionRepository.calculateBalanceDelta(a.getId());
        BigDecimal current = a.getInitialBalance().add(delta);
        return new AccountResponse(
                a.getId(),
                a.getDepartment().getId(),
                a.getDepartment().getName(),
                a.getName(),
                a.getType(),
                a.getCurrency(),
                a.getInitialBalance(),
                current
        );
    }
}