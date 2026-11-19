package com.example.DynamicCode.deploy.exception;

public class SshConnectionException extends RuntimeException {

    public SshConnectionException(Throwable cause) {
        super("Błąd połączenia SSH podczas wysyłki", cause);
    }
}
