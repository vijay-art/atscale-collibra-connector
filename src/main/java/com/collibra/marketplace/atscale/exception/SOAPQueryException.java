package com.collibra.marketplace.atscale.exception;

public class SOAPQueryException extends RuntimeException{
    public SOAPQueryException(String message) {
        super(message);
    }

    public SOAPQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
