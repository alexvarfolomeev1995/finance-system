package com.company.finance.financesystem.service;

import com.company.finance.financesystem.domain.entity.Account;
import com.company.finance.financesystem.domain.entity.Department;
import com.company.finance.financesystem.domain.entity.Transaction;
import com.company.finance.financesystem.domain.entity.User;
import com.company.finance.financesystem.domain.enums.TransactionStatus;
import com.company.finance.financesystem.domain.enums.TransactionType;
import com.company.finance.financesystem.dto.TransferRequest;
import com.company.finance.financesystem.exception.BusinessException;
import com.company.finance.financesystem.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CounterpartyRepository counterpartyRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditService auditService;

    @InjectMocks
    private TransactionService transactionService;

    private User author;
    private Account account;

    @BeforeEach
    void setUp() {
        author = User.builder().id(1L).email("test@test.com").fullName("Test").build();
        account = Account.builder().id(1L).initialBalance(BigDecimal.valueOf(1000)).build();
    }

    @Test
    void confirm_shouldThrow_whenTransactionIsNotDraft() {
        Transaction tx = Transaction.builder()
                .id(1L)
                .type(TransactionType.EXPENSE)
                .status(TransactionStatus.CONFIRMED)
                .amount(BigDecimal.valueOf(100))
                .account(account)
                .author(author)
                .build();

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(tx));

        assertThatThrownBy(() -> transactionService.confirm(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only DRAFT transactions can be confirmed");

        verify(transactionRepository, never()).save(any());
        verify(auditService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void confirm_shouldSucceed_whenTransactionIsDraftAndHasFunds() {
        Department dept = Department.builder().id(1L).name("IT").build();

        Transaction tx = Transaction.builder()
                .id(1L)
                .type(TransactionType.EXPENSE)
                .status(TransactionStatus.DRAFT)
                .amount(BigDecimal.valueOf(100))
                .currency("RUB")
                .transactionDate(LocalDate.now())
                .account(account)
                .author(author)
                .department(dept)
                .build();

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(tx));
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.calculateBalanceDelta(1L)).thenReturn(BigDecimal.valueOf(1000));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        var response = transactionService.confirm(1L);

        assertThat(response.status()).isEqualTo(TransactionStatus.CONFIRMED);
        verify(auditService).log(eq(1L), any(), eq("TRANSACTION"), eq(1L), anyString());
    }

    @Test
    void confirm_shouldThrow_whenInsufficientFunds() {
        Transaction tx = Transaction.builder()
                .id(1L)
                .type(TransactionType.EXPENSE)
                .status(TransactionStatus.DRAFT)
                .amount(BigDecimal.valueOf(999999))
                .account(account)
                .author(author)
                .build();

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(tx));
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.calculateBalanceDelta(1L)).thenReturn(BigDecimal.valueOf(1000));

        assertThatThrownBy(() -> transactionService.confirm(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    void reverse_shouldThrow_whenTransactionIsNotConfirmed() {
        Transaction tx = Transaction.builder()
                .id(1L)
                .type(TransactionType.EXPENSE)
                .status(TransactionStatus.DRAFT)
                .amount(BigDecimal.valueOf(100))
                .account(account)
                .author(author)
                .build();

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(tx));

        assertThatThrownBy(() -> transactionService.reverse(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only CONFIRMED transactions can be reversed");
    }

    @Test
    void reverse_shouldCreateReversalAndMarkOriginalReversed() {
        Department dept = Department.builder().id(1L).name("IT").build();

        Transaction original = Transaction.builder()
                .id(1L)
                .type(TransactionType.EXPENSE)
                .status(TransactionStatus.CONFIRMED)
                .amount(BigDecimal.valueOf(100))
                .currency("RUB")
                .transactionDate(LocalDate.now())
                .account(account)
                .author(author)
                .department(dept)
                .build();

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(original));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        var response = transactionService.reverse(1L, 1L);

        assertThat(original.getStatus()).isEqualTo(TransactionStatus.REVERSED);
        assertThat(response.reversalOfId()).isEqualTo(1L);
        assertThat(response.type()).isEqualTo(TransactionType.EXPENSE);
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(auditService).log(eq(1L), any(), eq("TRANSACTION"), eq(1L), anyString());
    }

    @Test
    void transfer_shouldThrow_whenSourceAndTargetAreSame() {
        TransferRequest req = new TransferRequest(
                1L, 1L, BigDecimal.valueOf(100), "RUB", LocalDate.now(), 1L, "test"
        );

        assertThatThrownBy(() -> transactionService.transfer(req, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Source and target accounts must differ");
    }
}