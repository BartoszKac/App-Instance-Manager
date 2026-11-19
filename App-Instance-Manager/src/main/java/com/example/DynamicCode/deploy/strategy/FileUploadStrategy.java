package com.example.DynamicCode.deploy.strategy;

import com.example.DynamicCode.deploy.enums.UploadStrategyType;

import java.util.Map;

public interface FileUploadStrategy {

    Map<String, byte[]> resolveFiles(Long id);

    UploadStrategyType getType();
}
