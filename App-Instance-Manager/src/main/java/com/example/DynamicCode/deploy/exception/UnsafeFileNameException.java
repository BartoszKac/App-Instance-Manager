package com.example.DynamicCode.deploy.exception;

public class UnsafeFileNameException extends RuntimeException {

    public UnsafeFileNameException(Long fileId) {
        super("Niedozwolona nazwa pliku o id " + fileId);
    }
}
