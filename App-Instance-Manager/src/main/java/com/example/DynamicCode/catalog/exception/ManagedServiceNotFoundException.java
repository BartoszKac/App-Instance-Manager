package com.example.DynamicCode.catalog.exception;

public class ManagedServiceNotFoundException extends RuntimeException {

    public ManagedServiceNotFoundException() {
        super("Nie ma takiego serwisu. Odśwież listę.");
    }
}
