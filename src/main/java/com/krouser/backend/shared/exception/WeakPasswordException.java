package com.krouser.backend.shared.exception;

import org.springframework.http.HttpStatus;

public class WeakPasswordException extends BusinessException {

    public WeakPasswordException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
