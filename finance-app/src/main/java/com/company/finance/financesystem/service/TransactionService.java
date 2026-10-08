package com.company.finance.financesystem.service;

import com.company.finance.financesystem.domain.entity.*;
import com.company.finance.financesystem.domain.enums.AuditAction;
import com.company.finance.financesystem.domain.enums.TransactionStatus;
import com.company.finance.financesystem.domain.enums.TransactionType;
import com.company.finance.financesystem.dto.TransactionRequest;
import com.company.finance.financesystem.dto.TransactionResponse;
import com.company.finance.financesystem.dto.TransferRequest;
import com.company.finance.financesystem.exception.BusinessException;
import com.company.finance.financesystem.exception.NotFoundException;
import com.company.finance.financesystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final CounterpartyRepository counterpartyRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public TransactionResponse create(TransactionRequest req, Long authorId) {
        validateCreateRequest(req);

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Author not found: " + authorId));
        Account account = accountRepository.findById(req.accountId())
                .orElseThrow(() -> new NotFoundException("Account not found: " + req.accountId()));
        Department department = departmentRepository.findById(req.departmentId())
                .orElseThrow(() -> new NotFoundException("Department not found: " + req.departmentId()));

        Account targetAccount = null;
        if (req.type() == TransactionType.TRANSFER) {
            targetAccount = accountRepository.findById(req.targetAccountId())
                    .orElseThrow(() -> new NotFoundException("Target account not found: " + req.targetAccountId()));
            if (targetAccount.getId().equals(account.getId())) {
                throw new BusinessException("Source and target accounts must differ");
            }
        }

        Category category = null;
        if (req.type() == TransactionType.INCOME || req.type() == TransactionType.EXPENSE) {
            category = categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found: " + req.categoryId()));
        }

        Counterparty counterparty = null;
        if (req.counterpartyId() != null) {
            counterparty = counterpartyRepository.findById(req.counterpartyId())
                    .orElseThrow(() -> new NotFoundException("Counterparty not found: " + req.counterpartyId()));
        }

        Transaction tx = Transaction.builder()
                .type(req.type())
                .status(TransactionStatus.DRAFT)
                .amount(req.amount())
                .currency(req.currency())
                .transactionDate(req.transactionDate())
                .account(account)
                .targetAccount(targetAccount)
                .category(category)
                .counterparty(counterparty)
                .department(department)
                .author(author)
                .description(req.description())
                .build();

        Transaction saved = transactionRepository.save(tx);

        auditService.log(authorId, AuditAction.CREATE, "TRANSACTION", saved.getId(),
                "Created " + saved.getType() + " amount=" + saved.getAmount());

        return toResponse(saved);
    }

    @Transactional
    public TransactionResponse confirm(Long id) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found: " + id));

        if (tx.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only DRAFT transactions can be confirmed. Current status: " + tx.getStatus());
        }

        if (tx.getType() == TransactionType.EXPENSE || tx.getType() == TransactionType.TRANSFER) {
            Account account = accountRepository.findByIdForUpdate(tx.getAccount().getId())
                    .orElseThrow(() -> new NotFoundException("Account not found"));
            BigDecimal delta = transactionRepository.calculateBalanceDelta(account.getId());
            BigDecimal currentBalance = account.getInitialBalance().add(delta);
            if (currentBalance.compareTo(tx.getAmount()) < 0) {
                throw new BusinessException("Insufficient funds. Current: " + currentBalance + ", required: " + tx.getAmount());
            }
        }

        tx.setStatus(TransactionStatus.CONFIRMED);
        Transaction saved = transactionRepository.save(tx);

        auditService.log(tx.getAuthor().getId(), AuditAction.CONFIRM, "TRANSACTION", saved.getId(),
                "Confirmed transaction amount=" + saved.getAmount());

        return toResponse(saved);
    }

    @Transactional
    public TransactionResponse reverse(Long id, Long authorId) {
        Transaction original = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found: " + id));

        if (original.getStatus() != TransactionStatus.CONFIRMED) {
            throw new BusinessException("Only CONFIRMED transactions can be reversed. Current status: " + original.getStatus());
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Author not found: " + authorId));

        Transaction reversal = Transaction.builder()
                .type(original.getType())
                .status(TransactionStatus.CONFIRMED)
                .amount(original.getAmount())
                .currency(original.getCurrency())
                .transactionDate(java.time.LocalDate.now())
                .account(original.getAccount())
                .targetAccount(original.getTargetAccount())
                .category(original.getCategory())
                .counterparty(original.getCounterparty())
                .department(original.getDepartment())
                .author(author)
                .reversalOf(original)
                .description("Reversal of transaction #" + original.getId() + ": " +
                        (original.getDescription() != null ? original.getDescription() : ""))
                .build();

        original.setStatus(TransactionStatus.REVERSED);
        transactionRepository.save(original);

        Transaction savedReversal = transactionRepository.save(reversal);

        auditService.log(authorId, AuditAction.REVERSE, "TRANSACTION", original.getId(),
                "Reversed by transaction #" + savedReversal.getId());

        return toResponse(savedReversal);
    }

    @Transactional
    public TransactionResponse transfer(TransferRequest req, Long authorId) {
        if (req.fromAccountId().equals(req.toAccountId())) {
            throw new BusinessException("Source and target accounts must differ");
        }

        Account from = accountRepository.findByIdForUpdate(req.fromAccountId())
                .orElseThrow(() -> new NotFoundException("Source account not found"));
        Account to = accountRepository.findByIdForUpdate(req.toAccountId())
                .orElseThrow(() -> new NotFoundException("Target account not found"));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Author not found: " + authorId));
        Department department = departmentRepository.findById(req.departmentId())
                .orElseThrow(() -> new NotFoundException("Department not found: " + req.departmentId()));

        BigDecimal delta = transactionRepository.calculateBalanceDelta(from.getId());
        BigDecimal fromBalance = from.getInitialBalance().add(delta);
        if (fromBalance.compareTo(req.amount()) < 0) {
            throw new BusinessException("Insufficient funds on source account. Current: " +
                    fromBalance + ", required: " + req.amount());
        }

        Transaction tx = Transaction.builder()
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.CONFIRMED)
                .amount(req.amount())
                .currency(req.currency())
                .transactionDate(req.transactionDate())
                .account(from)
                .targetAccount(to)
                .department(department)
                .author(author)
                .description(req.description())
                .build();

        Transaction saved = transactionRepository.save(tx);

        auditService.log(authorId, AuditAction.CREATE, "TRANSACTION", saved.getId(),
                "Transfer " + saved.getAmount() + " from account " + from.getId() + " to " + to.getId());

        return toResponse(saved);
    }

    @Transactional
    public void deleteDraft(Long id) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found: " + id));
        if (tx.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only DRAFT transactions can be deleted. Current status: " + tx.getStatus());
        }

        auditService.log(tx.getAuthor().getId(), AuditAction.DELETE_ATTEMPT, "TRANSACTION", tx.getId(),
                "Deleted DRAFT transaction");

        transactionRepository.delete(tx);
    }

    public TransactionResponse findById(Long id) {
        return transactionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Transaction not found: " + id));
    }

    public Page<TransactionResponse> findAllByDepartment(Long departmentId, Pageable pageable) {
        return transactionRepository.findAllByDepartmentId(departmentId, pageable).map(this::toResponse);
    }

    private void validateCreateRequest(TransactionRequest req) {
        switch (req.type()) {
            case INCOME, EXPENSE -> {
                if (req.categoryId() == null) {
                    throw new BusinessException("Category is required for " + req.type());
                }
                if (req.targetAccountId() != null) {
                    throw new BusinessException("targetAccountId must be null for " + req.type());
                }
            }
            case TRANSFER -> {
                if (req.targetAccountId() == null) {
                    throw new BusinessException("targetAccountId is required for TRANSFER");
                }
                if (req.categoryId() != null) {
                    throw new BusinessException("categoryId must be null for TRANSFER");
                }
            }
        }
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getType(),
                t.getStatus(),
                t.getAmount(),
                t.getCurrency(),
                t.getTransactionDate(),
                t.getAccount().getId(),
                t.getTargetAccount() != null ? t.getTargetAccount().getId() : null,
                t.getCategory() != null ? t.getCategory().getId() : null,
                t.getCounterparty() != null ? t.getCounterparty().getId() : null,
                t.getDepartment().getId(),
                t.getAuthor().getId(),
                t.getReversalOf() != null ? t.getReversalOf().getId() : null,
                t.getDescription(),
                t.getCreatedAt()
        );
    }
}