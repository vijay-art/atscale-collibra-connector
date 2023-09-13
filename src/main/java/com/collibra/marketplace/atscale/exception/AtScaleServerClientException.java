package com.collibra.marketplace.atscale.exception;

public class AtScaleServerClientException extends RuntimeException{
    public AtScaleServerClientException(String message) {
        super(message);
    }

    public AtScaleServerClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
