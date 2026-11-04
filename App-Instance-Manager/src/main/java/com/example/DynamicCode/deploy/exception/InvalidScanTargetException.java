package com.example.DynamicCode.deploy.exception;

public class InvalidScanTargetException extends RuntimeException {

    public InvalidScanTargetException() {
        super("Tego adresu nie można skanować. Musi należeć do podsieci z listy.");
    }
}
