package com.example.DynamicCode.service.launcher;

import com.example.DynamicCode.databaseservice.code.SourceCodeService;
import com.example.DynamicCode.factory.langugage.LanguageHandlerFactory;
import com.example.DynamicCode.databaseservice.code.CompiledCodeService;
import com.example.DynamicCode.model.entity.code.SourceCode;
import com.example.DynamicCode.service.prcoces.LanguageProces;
import com.example.DynamicCode.strategy.language.LanguageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppLauncherService {

    private final LanguageHandlerFactory languageHandlerFactory;
    private final SourceCodeService sourceCodeService;
    private final CompiledCodeService compiledCodeService;
    private final LanguageProces languageProces;

    public String launchApp(Long idMainClass) {
        try {
            log.info("Rozpoczynam procedurę uruchamiania dla idMainClass: {}", idMainClass);

            SourceCode sourceCode = sourceCodeService.getFileById(idMainClass);
            if (sourceCode == null) {
                throw new RuntimeException("Nie znaleziono pliku o ID: " + idMainClass);
            }

            String extension = sourceCode.getLanguage().getExtension();
            LanguageHandler handler = languageHandlerFactory.getHandler(extension);

            String mainFileName = sourceCode.getName();
            if (mainFileName.endsWith(extension)) {
                mainFileName = mainFileName.substring(0, mainFileName.length() - extension.length());
            }

            List<String> runCommand = handler.getRunCommand(mainFileName);
            log.info("Uruchamiam aplikację przez LanguageProces: {}", runCommand);

            int runExitCode = languageProces.runProcess(runCommand, "[URUCHOMIENIE]",String.valueOf(idMainClass));
            log.info("Aplikacja zakończyła działanie z kodem: {}", runExitCode);

            if (runExitCode != 0) {
                return "Aplikacja zakończyła się błędem (Exit code: " + runExitCode + "). Sprawdź logi na frontendzie.";
            }

            return "Aplikacja wykonała się pomyślnie! Pełne logi zostały przesłane na frontend.";

        } catch (Exception e) {
            log.error("Błąd krytyczny w AppLauncherService: {}", e.getMessage(), e);
            throw new RuntimeException("Nie udało się uruchomić aplikacji: " + e.getMessage(), e);
        }
    }
}