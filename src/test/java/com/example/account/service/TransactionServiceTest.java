package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import com.example.account.dto.TransactionDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import com.example.account.type.ErrorCode;
import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void successUseBalance() {
        // given
        AccountUser user = AccountUser.builder()
                .id(14L)
                .name("yuns")
                .build();
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000014")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(TransactionType.USE)
                        .transactionResultType(TransactionResultType.S)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .amount(1000L)
                        .balanceSnapshot(9000L)
                        .build());
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        // when
        TransactionDto transactionDto = transactionService.useBalance(1L, "1000000000", 200L);

        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(200L, captor.getValue().getAmount());
        assertEquals(9800L, captor.getValue().getBalanceSnapshot());
        assertEquals(TransactionResultType.S, transactionDto.getTransactionResultType());
        assertEquals(TransactionType.USE, transactionDto.getTransactionType());
        assertEquals(9000L, transactionDto.getBalanceSnapshot());
        assertEquals(1000L, transactionDto.getAmount());
    }

    @Test
    @DisplayName("Use Balance Failed : User Not Found")
    void useBalanceFailed_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        // when
        AccountException exception = assertThrows(AccountException.class, () -> transactionService.useBalance(1L, "1000000000", 1000L));

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("Use Balance Failed : Account Not Found")
    void useBalanceFailed_AccountNotFound() {
        // given
        AccountUser user = AccountUser.builder()
                .id(14L)
                .name("yuns")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException accountException = assertThrows(AccountException.class, () -> transactionService.useBalance(1L, "1000000012", 1000L));

        // then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("Use Balance Failed : User UnMatch")
    void useBalanceFailed_userUnMatch() {
        // given
        AccountUser user = AccountUser.builder()
                .id(14L)
                .name("yuns")
                .build();
        AccountUser otherUser = AccountUser.builder()
                .id(15L)
                .name("java")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(otherUser)
                        .balance(0L)
                        .accountNumber("1000000014")
                        .build()));

        // when
        AccountException accountException = assertThrows(AccountException.class, () -> transactionService.useBalance(1L, "1000000012", 1000L));

        // then
        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, accountException.getErrorCode());
    }

    @Test
    @DisplayName("Use Balance Failed : Already Unregistered Account")
    void useBalanceFailed_alreadyUnregisteredAccount() {
        // given
        AccountUser user = AccountUser.builder()
                .id(14L)
                .name("yuns")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .balance(0L)
                        .accountNumber("1000000014")
                        .build()));

        // when
        AccountException accountException = assertThrows(AccountException.class, () -> transactionService.useBalance(1L, "1000000012", 1000L));

        // then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, accountException.getErrorCode());
    }

    @Test
    @DisplayName("Exceed Amount")
    void exceedAmount_UseBalance() {
        // given
        AccountUser user = AccountUser.builder()
                .id(14L)
                .name("yuns")
                .build();
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .balance(1000L)
                .accountNumber("1000000014")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        // when
        // when
        AccountException accountException = assertThrows(AccountException.class, () -> transactionService.useBalance(1L, "1000000012", 100000L));

        // then
        assertEquals(ErrorCode.AMOUNT_EXCEED_BALANCE, accountException.getErrorCode());
        verify(transactionRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("Save Failed Use Transaction")
    void saveFailedUseTransaction() {
        // given
        AccountUser user = AccountUser.builder()
                .id(14L)
                .name("yuns")
                .build();
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000014")
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(TransactionType.USE)
                        .transactionResultType(TransactionResultType.S)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .amount(1000L)
                        .balanceSnapshot(9000L)
                        .build());
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        // when
        transactionService.saveFailedUseTransaction("1000000000", 200L);

        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(200L, captor.getValue().getAmount());
        assertEquals(10000L, captor.getValue().getBalanceSnapshot());
        assertEquals(TransactionResultType.F, captor.getValue().getTransactionResultType());
    }
}