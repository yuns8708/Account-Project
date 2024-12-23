package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND("User Not Found."),
    ACCOUNT_NOT_FOUND("Account Not Found."),
    USER_ACCOUNT_UN_MATCH("User Account Unmatch."),
    ACCOUNT_ALREADY_UNREGISTERED("Account Already Unregistered."),
    ACCOUNT_HAS_BALANCE("Account Has Balance."),
    MAX_ACCOUNT_PER_USER_10("Max Number of Account is 10."),
    AMOUNT_EXCEED_BALANCE("Amount Exceed Balance."),;

    private final String description;
}
