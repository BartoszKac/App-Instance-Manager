package com.example.DynamicCode.deploy.exception;

import com.example.DynamicCode.deploy.enums.UploadStrategyType;

public class NoFilesToTransferException extends RuntimeException {

    public NoFilesToTransferException(Long id, UploadStrategyType strategyType) {
        super("Brak plików do wysłania dla id " + id + " i strategii " + strategyType);
    }
}
