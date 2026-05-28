package com.example.DynamicCode.factory.deploy.sshfiiletransfer;


import com.example.DynamicCode.constants.deploy.UploadStrategyType;
import com.example.DynamicCode.strategy.sshfiiletransfer.FileUploadStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Component
public class FileUploadStrategyFactory {

    private final Map<UploadStrategyType, FileUploadStrategy> strategyMap;


    public FileUploadStrategyFactory(List<FileUploadStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        FileUploadStrategy::getType,
                        Function.identity()
                ));
        log.info("FileUploadStrategyFactory: Zarejestrowano {} strategii: {}",
                strategyMap.size(), strategyMap.keySet());
    }


    public FileUploadStrategy getStrategy(UploadStrategyType type) {
        FileUploadStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Brak zarejestrowanej strategii dla typu: " + type);
        }
        log.debug("FileUploadStrategyFactory: Zwracam strategię {} dla typu {}",
                strategy.getClass().getSimpleName(), type);
        return strategy;
    }
}
