package com.collibra.marketplace.atscale.exception;

public class GuidException extends RuntimeException{

    public GuidException(String message) {
        super(message);
    }

    public GuidException(String message, Throwable cause) {
        super(message, cause);
    }
}
