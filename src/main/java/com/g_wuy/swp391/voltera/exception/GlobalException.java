package com.g_wuy.swp391.voltera.exception;

import com.g_wuy.swp391.voltera.model.enums.ErrorCode;
import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {
    private final ErrorCode errorCode;

    public GlobalException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
