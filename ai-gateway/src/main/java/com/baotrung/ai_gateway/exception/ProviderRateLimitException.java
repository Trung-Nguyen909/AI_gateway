package com.baotrung.ai_gateway.exception;

public class ProviderRateLimitException extends RuntimeException {

    public ProviderRateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}