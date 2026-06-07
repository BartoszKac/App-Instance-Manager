package com.example.DynamicCode.controller.code;


import com.example.DynamicCode.model.entity.code.CompiledCode;
import com.example.DynamicCode.model.entity.code.SourceCode;
import com.example.DynamicCode.service.code.provider.ProviderCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/code")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CodeController {

    private final ProviderCodeService providerCodeService;

    // --- KOD ŹRÓDŁOWY ---

    @GetMapping("/source/main-class/{idMainClass}")
    public ResponseEntity<List<SourceCode>> getSourceCodes(@PathVariable Long idMainClass) {
        return ResponseEntity.ok(providerCodeService.getSourceCodesByMainClass(idMainClass));
    }

    @PostMapping("/source")
    public ResponseEntity<String> saveSourceCodes(@RequestBody List<SourceCode> sourceCodes) {
        System.out.println("Received source codes: " + sourceCodes);
        String path = providerCodeService.saveSourceCodeInSystem(sourceCodes);
        return ResponseEntity.ok(path);
    }

    @PutMapping("/source")
    public ResponseEntity<SourceCode> updateSourceCode(@RequestBody SourceCode sourceCode) {
        return ResponseEntity.ok(providerCodeService.updateSourceCode(sourceCode));
    }

    // --- KOMPILACJA ---

    @PostMapping("/compile/main-class/{idMainClass}")
    public ResponseEntity<Void> processCompilationResult(
            @PathVariable Long idMainClass,
            @RequestBody List<SourceCode> sourceFiles) {
        providerCodeService.processAndHandleCompilationResult(idMainClass, sourceFiles);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/compiled")
    public ResponseEntity<String> saveCompiledCodes(@RequestBody List<SourceCode> compiledCodes) {
        return ResponseEntity.ok(providerCodeService.saveCompiledCodeInSystem(compiledCodes));
    }

    @GetMapping("/compiled/main-class/{idMainClass}")
    public ResponseEntity<List<CompiledCode>> getCompiledCodes(@PathVariable Long idMainClass) {
        return ResponseEntity.ok(providerCodeService.getCompiledCodesByMainClass(idMainClass));
    }

    // --- CZYSZCZENIE ---

    @DeleteMapping("/project/{idMainClass}")
    public ResponseEntity<Void> deleteEntireProject(@PathVariable Long idMainClass) {
        providerCodeService.deleteEntireProjectByMainClass(idMainClass);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/source/all")
    public ResponseEntity<List<SourceCode>> getAllSourceCode() {
        return ResponseEntity.ok(providerCodeService.getAllSourceCodes());
    }

    @GetMapping("/source/main-class/all")
    public ResponseEntity<List<SourceCode>> getAllMainClassSourceCodes() {
        return ResponseEntity.ok(providerCodeService.getAllMainClassSourceCodes());
    }

    @PostMapping("/compile/all/main-class/{idMainClass}")
    public ResponseEntity<String> compileAllFilesFromMainClass(@PathVariable Long idMainClass) {
        return ResponseEntity.ok(providerCodeService.compileAllFilesFromMainClass(idMainClass));
    }
}