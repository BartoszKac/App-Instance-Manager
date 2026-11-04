package com.example.DynamicCode.deploy.exception;

public class NetworkScanFailedException extends RuntimeException {

    public NetworkScanFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
