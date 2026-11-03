package com.example.DynamicCode.deploy.exception;

public class NetworkScanInProgressException extends RuntimeException {

    public NetworkScanInProgressException() {
        super("Skan sieci już trwa. Poczekaj na jego koniec.");
    }
}
