package com.example.DynamicCode.service.code.compilation;

import com.example.DynamicCode.databaseservice.file.SourceFolderInTheDiskService;
import com.example.DynamicCode.factory.langugage.LanguageHandlerFactory;
import com.example.DynamicCode.mapper.code.MapperCode;
import com.example.DynamicCode.databaseservice.code.SourceCodeService;
import com.example.DynamicCode.model.entity.code.SourceCode;
import com.example.DynamicCode.service.code.prcoces.LanguageProces;
import com.example.DynamicCode.service.file.SavingDataInSystemService;
import com.example.DynamicCode.strategy.language.LanguageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompilationService {

    private final SourceCodeService sourceCodeService;
    private final LanguageHandlerFactory languageHandlerFactory;
    private final LanguageProces languageProces;
    private final MapperCode mapperCode;
    private final SavingDataInSystemService savingDataInSystemService;
    private final SourceFolderInTheDiskService sourceFolderInTheDiskService;


    public void compileAllFilesFromMainClass(Long idMainClass) {
        log.info("Rozpoczynam kompilację dla idMainClass: {}", idMainClass);

        SourceCode mainFile = sourceCodeService.getFileById(idMainClass);
        LanguageHandler handler = languageHandlerFactory.getHandler(mainFile.getLanguage().getExtension());

        List<SourceCode> sourceCodes = sourceCodeService.getAllFilesFromMainClass(idMainClass);
        if (sourceCodes == null || sourceCodes.isEmpty()) {
            log.warn("Brak plików do skompilowania dla idMainClass: {}", idMainClass);
            return;
        }

        String projectFolderPath = sourceFolderInTheDiskService.getFoldersFromSourceCodeId(mainFile.getId()).getPath();
        List<String> allFileNames = mapperCode.toFileNameList(sourceCodes);
        String mainFileNameWithoutExt = mainFile.getName().replace(mainFile.getLanguage().getExtension(), "");

        List<String> compileCommand = handler.getCompileCommand(mainFileNameWithoutExt, allFileNames);

        try {
            int exitCode = languageProces.runProcessInDirectory(compileCommand, "[KOMPILACJA]", projectFolderPath);

            if (exitCode != 0) {
                log.error("Kompilacja nie powiodła się. Kod wyjścia: {}", exitCode);
                return;
            }

            log.info("Kompilacja sukces! Zapisywanie wyników...");
            savingDataInSystemService.handleCompilationResult(idMainClass, sourceCodes);

        } catch (Exception e) {
            log.error("Błąd krytyczny podczas kompilacji dla idMainClass: {}", idMainClass, e);
        }
    }






    private boolean isCollectionEmpty(List<?> collection) {
        return collection == null || collection.isEmpty();
    }
}