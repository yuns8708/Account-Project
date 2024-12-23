package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance) {
        // 사용자 있는지 확인
        AccountUser accountUser = accountUserRepository.findById(userId).orElseThrow(
                () -> new AccountException(ErrorCode.USER_NOT_FOUND)
        );

        validateCreateAccount(accountUser);

        // 계좌 번호 생성
        String newAccountNumber = accountRepository.findFirstByOrderByIdDesc()
                .map(account -> (Integer.parseInt(account.getAccountNumber())) + 1 + "")
                .orElse("1000000000");

        // 계좌 저장, 정보 전달
        return AccountDto.fromEntity(
                accountRepository.save(Account.builder()
                .accountUser(accountUser)
                .accountStatus(AccountStatus.IN_USE)
                .accountNumber(newAccountNumber)
                .balance(initialBalance)
                .registeredAt(LocalDateTime.now())
                .build())
        );
    }

    private void validateCreateAccount(AccountUser accountUser) {
        // 계정 10개이면 제한
        if(accountRepository.countByAccountUser(accountUser) == 10) {
            throw new AccountException(ErrorCode.MAX_ACCOUNT_PER_USER_10);
        }
    }

    @Transactional
    public Account getAccount(Long id) {
        return accountRepository.findById(id).get();
    }

    @Transactional
    public AccountDto deleteAccount(Long userId, String accountNumber) {
        // 사용자 있는지 확인
        AccountUser accountUser = accountUserRepository.findById(userId).orElseThrow(
                () -> new AccountException(ErrorCode.USER_NOT_FOUND)
        );

        Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(
                () -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND)
        );

        // 삭제 가능한 계좌인지 검사
        validateDeleteAccount(accountUser, account);

        account.setAccountStatus(AccountStatus.UNREGISTERED);
        account.setUnRegisteredAt(LocalDateTime.now());

        accountRepository.save(account);
        return AccountDto.fromEntity(account);
    }

    private void validateDeleteAccount(AccountUser accountUser, Account account) {
        // 사용자 계좌와 삭제하려는 계좌가 다를때
        if (!Objects.equals(accountUser.getId(), account.getAccountUser().getId())) {
            throw new AccountException(ErrorCode.USER_ACCOUNT_UN_MATCH);
        }

        // 삭제하려는 계좌가 이미 해지되어있을때
        if (account.getAccountStatus() == AccountStatus.UNREGISTERED) {
            throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
        }

        // 삭제하려는 계좌에 잔액이 남아있을 때
        if (account.getBalance() > 0) {
            throw new AccountException(ErrorCode.ACCOUNT_HAS_BALANCE);
        }
    }

    @Transactional
    public List<AccountDto> getAccountsByUserId(Long userId) {
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));

        return accountRepository.findByAccountUser(accountUser).stream()
                .map(AccountDto::fromEntity)
                .collect(Collectors.toList());
    }
}
