package com.example.DynamicCode.deploy.exception;

public class RemoteWriteException extends RuntimeException {

    public RemoteWriteException(Throwable cause) {
        super("Błąd zapisu pliku przez SFTP", cause);
    }
}
