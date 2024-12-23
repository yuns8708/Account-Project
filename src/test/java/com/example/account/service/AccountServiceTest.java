package com.example.account.service;

import com.example.account.domain.AccountStatus;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.example.account.domain.Account;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    // accountService를 사용하기 위해 필요한 accountRepository를 가상으로 생성
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccountSuccess() {
        // given
        AccountUser user = AccountUser.builder()
                .id(14L)
                .name("yuns")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                                .accountNumber("1000000014")
                        .build()));
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                                .accountUser(user)
                                .accountNumber("1000000014")
                                .build());
        // when
        AccountDto accountDto = accountService.createAccount(1L, 1000L);

        // then
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(14L, accountDto.getUserId());
        assertEquals("1000000015", captor.getValue().getAccountNumber());
    }

    @Test
    void deleteAccountSuccess() {
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
                                .balance(0L)
                        .accountNumber("1000000014")
                        .build()));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto accountDto = accountService.deleteAccount(1L, "1000000014");

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(14L, accountDto.getUserId());
    }

    @Test
    @DisplayName("Delete Account Failed : User Not Found")
    void deleteAccountFailed_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        AccountException accountException = assertThrows(AccountException.class, () -> accountService.deleteAccount(1L, "1000000012"));

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("Delete Account Failed : Account Not Found")
    void deleteAccountFailed_AccountNotFound() {
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
        AccountException accountException = assertThrows(AccountException.class, () -> accountService.deleteAccount(1L, "1000000012"));

        // then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("Delete Account Failed : User UnMatch")
    void deleteAccountFailed_userUnMatch() {
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
        AccountException accountException = assertThrows(AccountException.class, () -> accountService.deleteAccount(1L, "1000000012"));

        // then
        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, accountException.getErrorCode());
    }

    @Test
    @DisplayName("Delete Account Failed : Account Has Balance")
    void deleteAccountFailed_accountHasBalance() {
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
                        .balance(1000L)
                        .accountNumber("1000000014")
                        .build()));

        // when
        AccountException accountException = assertThrows(AccountException.class, () -> accountService.deleteAccount(1L, "1000000012"));

        // then
        assertEquals(ErrorCode.ACCOUNT_HAS_BALANCE, accountException.getErrorCode());
    }

    @Test
    @DisplayName("Delete Account Failed : Already Unregistered Account")
    void deleteAccountFailed_alreadyUnregisteredAccount() {
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
        AccountException accountException = assertThrows(AccountException.class, () -> accountService.deleteAccount(1L, "1000000012"));

        // then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, accountException.getErrorCode());
    }

    @Test
    void createFirstAccount() {
        // given
        AccountUser user = AccountUser.builder()
                .id(14L)
                .name("yuns")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty());
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000014")
                        .build());
        // when
        AccountDto accountDto = accountService.createAccount(1L, 1000L);

        // then
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(14L, accountDto.getUserId());
        assertEquals("1000000000", captor.getValue().getAccountNumber());

    }

    @Test
    @DisplayName("Account Create Failed : User Not Found")
    void createAccount_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        // when
        AccountException exception = assertThrows(AccountException.class, () -> accountService.createAccount(1L, 1000L));

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("Max Number Of Account is 10")
    void createAccount_maxAccount10() {
        // given
        AccountUser accountUser = AccountUser.builder()
                .id(15L)
                .name("yuns")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));
        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);

        // when
        AccountException exception = assertThrows(AccountException.class, () -> accountService.createAccount(1L, 1000L));

        // then
        assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_10, exception.getErrorCode());
    }

    @Test
    @DisplayName("mock을 이용한 계좌 조회 성공")
    void accountTest() {
        // given
        given(accountRepository.findById(anyLong())).willReturn(Optional.of(Account.builder().accountStatus(AccountStatus.UNREGISTERED).accountNumber("6789")
                .build()));
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);

        // when
        Account account = accountService.getAccount(6789L);

        // then
        // account가져올때 repository가 findById를 한 번 호출했는지 확인
        verify(accountRepository, times(1)).findById(captor.capture());
//        verify(accountRepository, times(0)).save(any());

        assertEquals(6789L, captor.getValue());
        assertEquals("6789", account.getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, account.getAccountStatus());
    }

    @Test
    void successGetAccountsByUserId() {
        // given
        AccountUser user = AccountUser.builder()
                .id(15L)
                .name("yuns")
                .build();
        List<Account> accounts = Arrays.asList(
                Account.builder()
                        .accountUser(user)
                        .accountNumber("1234123412")
                        .balance(100L)
                        .build(),
                Account.builder()
                        .accountUser(user)
                        .accountNumber("1212121212")
                        .balance(100L)
                        .build(),
                Account.builder()
                        .accountUser(user)
                        .accountNumber("1231231231")
                        .balance(100L)
                        .build()
        );
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountUser(any()))
                .willReturn(accounts);

        // when
        List<AccountDto> accountDtos = accountService.getAccountsByUserId(1L);

        // then
        assertEquals(3, accountDtos.size());
        assertEquals("1234123412", accountDtos.get(0).getAccountNumber());
        assertEquals("1212121212", accountDtos.get(1).getAccountNumber());
        assertEquals("1231231231", accountDtos.get(2).getAccountNumber());
    }

    @Test
    void getAccountFailed() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        AccountException accountException = assertThrows(AccountException.class, () -> accountService.getAccountsByUserId(1L));

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }

//    @Test
//    void test1() {
////        String str = "hi";
////        assertEquals("hii", str);
//        accountService.createAccount(12L, 1000L);
//        Account account = accountService.getAccount(1L);
//
//        assertEquals("1234", account.getAccountNumber());
//        assertEquals(AccountStatus.IN_USE, account.getAccountStatus());
//    }

}