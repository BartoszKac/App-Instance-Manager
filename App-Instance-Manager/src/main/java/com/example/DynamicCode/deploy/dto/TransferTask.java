package com.example.DynamicCode.deploy.dto;

import com.example.DynamicCode.deploy.enums.UploadStrategyType;
import jakarta.validation.constraints.NotNull;

public record TransferTask(
        @NotNull(message = "mainClassId jest wymagane")
        Long mainClassId,

        @NotNull(message = "configurationId jest wymagane")
        Long configurationId,

        @NotNull(message = "uploadStrategyType jest wymagane")
        UploadStrategyType uploadStrategyType
) {
}