package com.stegovault.exception;

/**
 * Rzuca wyjatek gdy nie powiedzie sie enkrypcja lub dekrypcja
 */
public class CryptoException extends Exception {
    
    public CryptoException(String message)
    {
        super(message);
    }
    public CryptoException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
