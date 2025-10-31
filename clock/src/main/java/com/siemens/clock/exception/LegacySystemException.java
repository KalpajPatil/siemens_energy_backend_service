package com.siemens.clock.exception;

public class LegacySystemException extends RuntimeException {
    public LegacySystemException(String message) {
        super(message);
    }

    public LegacySystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
