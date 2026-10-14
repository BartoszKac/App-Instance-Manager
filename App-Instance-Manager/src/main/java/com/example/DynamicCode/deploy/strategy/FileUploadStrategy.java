package com.example.DynamicCode.deploy.strategy;

import com.example.DynamicCode.deploy.enums.UploadStrategyType;

import java.util.Map;


public interface FileUploadStrategy {


    Map<String, String> resolveFiles(Long mainClassId);

    UploadStrategyType getType();
}