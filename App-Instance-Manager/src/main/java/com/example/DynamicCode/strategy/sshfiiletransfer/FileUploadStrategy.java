package com.example.DynamicCode.strategy.sshfiiletransfer;

import com.example.DynamicCode.constants.deploy.UploadStrategyType;

import java.util.Map;


public interface FileUploadStrategy {


    Map<String, String> resolveFiles(Long mainClassId);

    UploadStrategyType getType();
}