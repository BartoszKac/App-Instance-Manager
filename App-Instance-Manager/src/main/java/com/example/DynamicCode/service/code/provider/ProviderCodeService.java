package com.example.DynamicCode.service.code.provider;

import com.example.DynamicCode.databaseservice.code.CompiledCodeService;
import com.example.DynamicCode.databaseservice.code.SourceCodeService;
import com.example.DynamicCode.model.entity.code.CompiledCode;
import com.example.DynamicCode.model.entity.code.SourceCode;
import com.example.DynamicCode.service.file.SavingDataInSystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderCodeService {

    private final SourceCodeService sourceCodeService;
    private final CompiledCodeService compiledCodeService;
    private final SavingDataInSystemService savingDataInSystemService; // Wstrzykujemy Twój serwis dyskowy

    // --- SEKCJA KODU ŹRÓDŁOWEGO (ZAPIS SYSTEMOWY I POBIERANIE) ---

    /**
     * Pobiera kody źródłowe z bazy (np. do załadowania w edytorze na front-endzie).
     */
    public List<SourceCode> getSourceCodesByMainClass(Long idMainClass) {
        log.info("[Provider] Pobieranie kodu źródłowego z bazy dla idMainClass: {}", idMainClass);
        return sourceCodeService.getAllFilesFromMainClass(idMainClass);
    }

    /**
     * Główna metoda zapisu kodu źródłowego przysyłanego z front-endu.
     * Zapisuje pliki na dysku, generuje ścieżkę i rejestruje wszystko w bazie danych.
     * * @return Zwraca unikalną ścieżkę na dysku (przydatne dla kompilatora).
     */
    public String saveSourceCodeInSystem(List<SourceCode> sourceCodes) {
        log.info("[Provider] Wywołanie pełnego zapisu kodu źródłowego w systemie (Dysk + DB) dla {} plików.", sourceCodes.size());
        return savingDataInSystemService.saveSourceCodeIntheSystem(sourceCodes);
    }

    /**
     * Aktualizacja pojedynczego pliku źródłowego (gdy użytkownik kliknie "Zapisz" w edytorze).
     */
    public SourceCode updateSourceCode(SourceCode sourceCode) {
        log.info("[Provider] Aktualizacja pliku źródłowego o ID: {}", sourceCode.getId());
        return sourceCodeService.updateFile(sourceCode);
    }

    // --- SEKCJA KOMPILACJI (PROCESOWANIE I REZULTATY) ---

    /**
     * Wywoływane, gdy proces kompilacji się zakończy.
     * Odczytuje pliki z dysku, mapuje je i zapisuje struktury skompilowane.
     */
    public void processAndHandleCompilationResult(Long idMainClass, List<SourceCode> sourceFiles) {
        log.info("[Provider] Przetwarzanie wyniku kompilacji dla idMainClass: {}", idMainClass);
        savingDataInSystemService.handleCompilationResult(idMainClass, sourceFiles);
    }

    /**
     * Bezpośredni zapis skompilowanego kodu (jeśli front-end wysyła gotowy bytecode).
     */
    public String saveCompiledCodeInSystem(List<SourceCode> compiledCodes) {
        log.info("[Provider] Wywołanie zapisu skompilowanych plików w systemie.");
        return savingDataInSystemService.SaveCompiledCodeIntheSystem(compiledCodes);
    }

    /**
     * Pobiera skompilowane pliki z bazy (np. przed uruchomieniem aplikacji).
     */
    public List<CompiledCode> getCompiledCodesByMainClass(Long idMainClass) {
        log.info("[Provider] Pobieranie skompilowanego kodu z bazy dla idMainClass: {}", idMainClass);
        return compiledCodeService.getAllFilesFromMainClass(idMainClass);
    }

    // --- SEKCJA CZYSZCZENIA ---

    /**
     * Usuwa z bazy dane o projekcie (źródła i skompilowane).
     * W przyszłości możesz tu też dodać usuwanie fizycznego folderu z dysku przez fileService!
     */
    public void deleteEntireProjectByMainClass(Long idMainClass) {
        log.warn("[Provider] Kompleksowe usuwanie projektu z bazy dla idMainClass: {}", idMainClass);
        compiledCodeService.deleteAllFilesByMainClass(idMainClass);
        sourceCodeService.deleteAllFilesByMainClass(idMainClass);
    }
}