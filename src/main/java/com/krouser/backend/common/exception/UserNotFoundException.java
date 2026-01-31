package com.krouser.backend.common.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException(String identifierType, String identifier) {
        super("User not found with " + identifierType + ": " + identifier, HttpStatus.NOT_FOUND);
    }
}
