package com.example.DynamicCode.code.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidFileNameException extends IllegalArgumentException {

    public InvalidFileNameException(String fileName) {
        super("Niedozwolona nazwa pliku: '" + fileName + "'. Dozwolone: litery, cyfry, '.', '_', '-' (bez ścieżek).");
    }
}
