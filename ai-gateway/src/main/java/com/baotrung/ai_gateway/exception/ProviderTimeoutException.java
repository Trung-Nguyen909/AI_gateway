package com.baotrung.ai_gateway.exception;

public class ProviderTimeoutException extends RuntimeException {

    public ProviderTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}