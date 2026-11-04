package com.example.DynamicCode.deploy.exception;

public class InvalidSubnetException extends RuntimeException {

    public InvalidSubnetException() {
        super("Tej podsieci nie można skanować. Wybierz podsieć z listy.");
    }
}
