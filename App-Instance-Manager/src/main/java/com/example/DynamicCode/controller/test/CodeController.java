package com.example.DynamicCode.controller.test;

import com.example.DynamicCode.model.entity.code.CompiledCode; // NOWOŚĆ
import com.example.DynamicCode.databaseservice.code.SourceCodeService;
import com.example.DynamicCode.databaseservice.code.CompiledCodeService; // NOWOŚĆ
import com.example.DynamicCode.model.entity.code.SourceCode;
import com.example.DynamicCode.service.code.compilation.CompilationService;
import com.example.DynamicCode.service.code.launcher.AppLauncherService;
import com.example.DynamicCode.service.file.SavingDataInSystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/code")
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class CodeController {

    private final SourceCodeService sourceCodeService;
    private final CompiledCodeService compiledCodeService;
    private final CompilationService compilationService;
    private final AppLauncherService appLauncherService;
    private final SavingDataInSystemService savingDataInSystemService;

    /**
     * Endpoint 1: Zapis wielu plików do bazy danych
     * POST http://localhost:8080/api/code/save-multiple
     */
    @PostMapping("/save-multiple")
    public ResponseEntity<String> saveMultipleFiles(@RequestBody List<SourceCode> codeRequests) {
        log.info("Otrzymano żądanie zapisu listy plików. Liczba plików: {}", codeRequests.size());
        try {
            savingDataInSystemService.saveSourceCodeIntheSystem(codeRequests);
            return ResponseEntity.ok("Wszystkie pliki zostały pomyślnie zapisane w bazie danych!");
        } catch (Exception e) {
            log.error("Błąd podczas zapisu plików do bazy: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Nie udało się zapisać plików: " + e.getMessage());
        }
    }

    /**
     * Endpoint 2: Kompilacja plików dla podanego idMainClass
     * POST http://localhost:8080/api/code/compile/{idMainClass}
     */
    @PostMapping("/compile/{idMainClass}")
    public ResponseEntity<String> compileFiles(@PathVariable Long idMainClass) {
        log.info("Otrzymano żądanie kompilacji dla idMainClass: {}", idMainClass);
        try {
            compilationService.compileAllFilesFromMainClass(idMainClass);
            return ResponseEntity.ok("Proces kompilacji dla idMainClass " + idMainClass + " został zakończony sukcesem.");
        } catch (IllegalArgumentException e) {
            log.warn("Błąd biznesowy walidacji: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Błąd krytyczny podczas wywołania kompilacji: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Wystąpił błąd podczas procesu kompilacji.");
        }
    }

    /**
     * Endpoint 3: Pobieranie wszystkich skompilowanych plików (.class) dla danego projektu
     * GET http://localhost:8080/api/code/compiled/{idMainClass}
     */
    @GetMapping("/compiled/{idMainClass}")
    public ResponseEntity<List<CompiledCode>> getAllCompiledFiles(@PathVariable Long idMainClass) {
        log.info("Otrzymano żądanie pobrania skompilowanych plików dla idMainClass: {}", idMainClass);
        try {
            List<CompiledCode> compiledFiles = compiledCodeService.getAllFilesFromMainClass(idMainClass);
            return ResponseEntity.ok(compiledFiles);
        } catch (Exception e) {
            log.error("Błąd podczas pobierania skompilowanych plików z bazy danych: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/launch/{idMainClass}")
    public ResponseEntity<String> launchApp(@PathVariable Long idMainClass) {
        log.info("Otrzymano żądanie uruchomienia aplikacji dla idMainClass: {}", idMainClass);
        try {
            String result = appLauncherService.launchApp(idMainClass);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Błąd krytyczny podczas uruchamiania aplikacji: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Nie udało się uruchomić aplikacji: " + e.getMessage());
        }
    }

    /**
     * Endpoint 5: Pobieranie wszystkich zapisanych plików SourceCode wraz z ich ID
     * GET http://localhost:8888/api/code/all-requests
     */
    @GetMapping("/all-requests")
    public ResponseEntity<List<SourceCode>> getAllCodeRequests() {
        log.info("Otrzymano żądanie pobrania wszystkich zapisanych plików SourceCode.");
        try {
            // Zakładam, że Twój codeStorageService posiada metodę pobierającą wszystko, np. getAllFiles() lub findAll()
            List<SourceCode> allRequests = sourceCodeService.getAllFilesFromMainClass();

            if (allRequests.isEmpty()) {
                log.warn("Baza danych SourceCode jest pusta.");
                return ResponseEntity.noContent().build();
            }

            log.info("Pomyślnie pobrano {} plików SourceCode.", allRequests.size());
            return ResponseEntity.ok(allRequests);
        } catch (Exception e) {
            log.error("Błąd podczas pobierania wszystkich plików SourceCode: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}