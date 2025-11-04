package com.siemens.clock.exception;

public class NonRetryableClientException extends RuntimeException {
    public NonRetryableClientException(String message) {
        super(message);
    }
}
