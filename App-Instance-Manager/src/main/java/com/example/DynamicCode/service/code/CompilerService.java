package com.example.DynamicCode.service.code;

import com.example.DynamicCode.factory.langugage.LanguageHandlerFactory;
import com.example.DynamicCode.mapper.code.MapperCode;
import com.example.DynamicCode.mapper.file.MapperFile;
import com.example.DynamicCode.model.entity.code.CompiledCode;
import com.example.DynamicCode.model.dto.file.CodeFileDto;
import com.example.DynamicCode.databaseservice.code.SourceCodeService;
import com.example.DynamicCode.databaseservice.code.CompiledCodeService;
import com.example.DynamicCode.model.entity.code.SourceCode;
import com.example.DynamicCode.service.prcoces.LanguageProces;
import com.example.DynamicCode.file.FilesSave;
import com.example.DynamicCode.strategy.language.LanguageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompilerService {

    private final SourceCodeService sourceCodeService;
    private final CompiledCodeService compiledCodeService;
    private final LanguageHandlerFactory languageHandlerFactory;
    private final LanguageProces languageProces;
    private final MapperCode mapperCode;
    private final MapperFile mapperFile;
    private final FilesSave filesSave;

    public void compileAllFilesFromMainClass(Long idMainClass) {
        log.info("Rozpoczynam proces kompilacji dla idMainClass: {}", idMainClass);

        List<SourceCode> sourceCodes = sourceCodeService.getAllFilesFromMainClass(idMainClass);
        if (isCollectionEmpty(sourceCodes)) {
            log.warn("Brak plików do skompilowania dla idMainClass: {}", idMainClass);
            return;
        }


        List<String> allFileNames = mapperCode.toFileNameList(sourceCodes);

        SourceCode mainFile = sourceCodeService.getFileById(idMainClass);
        LanguageHandler handler = languageHandlerFactory.getHandler(mainFile.getLanguage().getExtension());

        executeCompilation(mainFile, allFileNames, handler, idMainClass, sourceCodes);
    }

    private void executeCompilation(SourceCode mainFile, List<String> allFileNames, LanguageHandler handler, Long idMainClass, List<SourceCode> sourceFiles) {
        System.out.println("------------------------------------------------------------------------");
        System.out.println("[EXECUTE-COMPILATION] Rozpoczęcie procedury executeCompilation...");
        System.out.println("[EXECUTE-COMPILATION] ID Projektu (idMainClass): " + idMainClass);
        System.out.println("[EXECUTE-COMPILATION] Nazwa pliku głównego: " + mainFile.getName());
        System.out.println("------------------------------------------------------------------------");

        try {
            String mainFileNameWithoutExt = mainFile.getName().replace(mainFile.getLanguage().getExtension(), "");
            List<String> compileCommand = handler.getCompileCommand(mainFileNameWithoutExt, allFileNames);

            System.out.println("[EXECUTE-COMPILATION] [KOMENDA] Przygotowane polecenie kompilacji:");
            System.out.println("                      " + compileCommand);

            System.out.println("[EXECUTE-COMPILATION] Uruchamianie zewnętrznego procesu kompilacji...");
            int exitCode = languageProces.runProcess(compileCommand, "[KOMPILACJA]",String.valueOf(mainFile.getId()));

            System.out.println("[EXECUTE-COMPILATION] [WYNIK PROCESU] Kod wyjścia (exitCode): " + exitCode);

            if (exitCode == 0) {
                System.out.println("[EXECUTE-COMPILATION] Sukces procesu! Przechodzę do handleCompilationResult...");
            } else {
                System.out.println("[EXECUTE-COMPILATION] [OSTRZEŻENIE] Proces zwrócił kod błędu: " + exitCode);
            }

            handleCompilationResult(exitCode, idMainClass, sourceFiles);

        } catch (Exception e) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("[EXECUTE-COMPILATION] [BŁĄD KRYTYCZNY] Wystąpił wyjątek podczas kompilacji!");
            System.out.println("[EXECUTE-COMPILATION] idMainClass: " + idMainClass);
            System.out.println("[EXECUTE-COMPILATION] Treść błędu: " + e.getMessage());
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            e.printStackTrace(); // Drukuje cały ślad stosu (stacktrace) w konsoli, tak jak log.error
        }
    }


    private void handleCompilationResult(int exitCode, Long idMainClass, List<SourceCode> sourceFiles) {
        if (exitCode != 0) {
            log.error("Kompilacja nie powiodła się. Kod wyjścia: {}", exitCode);
            return;
        }

        log.info("Kompilacja zakończona sukcesem dla idMainClass: {}. Czytam binaria z dysku...", idMainClass);

        List<CodeFileDto> rawBinaryFiles = filesSave.readFilesFromDisk(idMainClass);

        if (rawBinaryFiles.isEmpty()) {
            log.warn("Przerwano mapowanie. Brak plików binarnych do przetworzenia.");
            return;
        }

        List<CompiledCode> compiledCodes = mapperFile.toCompileCodeList(sourceFiles, rawBinaryFiles);

        if (!compiledCodes.isEmpty()) {
            compiledCodeService.saveAllFilesToDb(compiledCodes);
        }
    }

    private boolean isCollectionEmpty(List<?> collection) {
        return collection == null || collection.isEmpty();
    }
}