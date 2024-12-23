package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INVALID_REQUEST("잘못된 요청입니다."),
    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
    ACCOUNT_NOT_FOUND("계좌를 찾을 수 없습니다."),
    USER_ACCOUNT_UN_MATCH("계좌의 소유주가 사용자와 다릅니다."),
    ACCOUNT_ALREADY_UNREGISTERED("계좌가 이미 해지되었습니다."),
    ACCOUNT_HAS_BALANCE("해지할 계좌에 잔액이 남아있습니다."),
    MAX_ACCOUNT_PER_USER_10("최대 계좌 수는 10개입니다."),
    AMOUNT_EXCEED_BALANCE("거래 금액이 잔액을 초과합니다."),
    TRANSACTION_NOT_FOUND("거래를 찾을 수 없습니다."),
    TRANSACTION_ACCOUNT_UN_MATCH("거래 사용자가 일치하지 않습니다."),
    CANCEL_MUST_FULLY("부분 취소는 불가능합니다."),
    TOO_OLD_ORDER_TO_CANCEL("1년이 지난 거래는 취소 불가능합니다.");

    private final String description;
}
