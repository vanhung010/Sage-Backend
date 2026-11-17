package com.sagares.saga_res_api.common.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final String error;
    private final HttpStatus status;

    public BusinessException(String code, String message) {
        this(HttpStatus.CONFLICT, code, message);
    }

    public BusinessException(HttpStatus status, String error, String message) {
        super(message);
        this.status = status;
        this.error = error;
    }
}