package com.krouser.backend.common.exception;

import org.springframework.http.HttpStatus;

public class AccountLockedException extends BusinessException {

    public AccountLockedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
