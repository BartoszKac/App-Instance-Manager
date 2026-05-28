package com.example.DynamicCode.factory.deploy.provider;

import com.example.DynamicCode.constants.deploy.UploadStrategyType;
import com.example.DynamicCode.databaseservice.DataBaseProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProgramLoaderFactory {


    private final Map<String, DataBaseProvider<?>> providers;


    public DataBaseProvider<?> getProvider(UploadStrategyType type) {
        log.info("ProgramLoaderFactory: Pobieram dostawcę bazy danych dla strategii: {}", type);

        if (type == null) {
            log.error("ProgramLoaderFactory: Typ strategii nie może być nullem!");
            throw new IllegalArgumentException("UploadStrategyType cannot be null");
        }

        return switch (type) {
            case SOURCE_CODE -> getBeanSafely("sourceCodeService");
            case COMPILED_CODE -> getBeanSafely("compiledCodeService");
            case REMOTE_EXECUTABLE -> getBeanSafely("remoteExecutableService");
        };
    }


    private DataBaseProvider<?> getBeanSafely(String beanName) {
        DataBaseProvider<?> provider = providers.get(beanName);
        if (provider == null) {
            log.error("ProgramLoaderFactory: Nie znaleziono zarejestrowanego komponentu o nazwie: {}", beanName);
            throw new IllegalStateException("Missing bean dependency for: " + beanName);
        }
        return provider;
    }
}