package com.example.account.controller;

import com.example.account.domain.Account;
import com.example.account.dto.AccountDto;
import com.example.account.dto.AccountInfo;
import com.example.account.dto.CreateAccount;
import com.example.account.dto.DeleteAccount;
import com.example.account.repository.AccountUserRepository;
import com.example.account.service.AccountService;
//import com.example.account.service.RedisTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
public class AccountController {
    private final AccountService accountService;
    private final AccountUserRepository accountUserRepository;
//    private final RedisTestService redisTestService;

//    @GetMapping("/get-lock")
//    public String getLock() {
//        return redisTestService.getLock();
//    }

    @PostMapping("/account")
    public CreateAccount.Response createAccount(@RequestBody @Valid CreateAccount.Request request) {
        return CreateAccount.Response.from(
                accountService.createAccount(
                        request.getUserId(), request.getInitialBalance()
                )
        );
    }

    @DeleteMapping("/account")
    public DeleteAccount.Response deleteAccount(@RequestBody @Valid DeleteAccount.Request request) {
        return DeleteAccount.Response.from(
                accountService.deleteAccount(
                        request.getUserId(), request.getAccountNumber()
                )
        );
    }

    // 계좌 가져오기
    @GetMapping("/account")
    public List<AccountInfo> getAccountsByUserId(@RequestParam("user_id") Long userId) {
        return accountService.getAccountsByUserId(userId).stream().map(accountDto -> AccountInfo.builder()
                .accountNumber(accountDto.getAccountNumber())
                .balance(accountDto.getBalance())
                .build())
                .collect(Collectors.toList());
    }

//    @GetMapping("/account/{id}")
//    public AccountDto getAccount(@PathVariable Long id) {
//
//        return accountService.getAccount(id);
//    }

}
