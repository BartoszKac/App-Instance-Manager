package com.example.DynamicCode.transfer.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DirectUploadException extends RuntimeException {

    private final HttpStatus status;

    public DirectUploadException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
