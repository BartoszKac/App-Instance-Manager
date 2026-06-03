package com.example.DynamicCode.controller.process;

import com.example.DynamicCode.constants.code.LanguageType;
import com.example.DynamicCode.model.entity.procces.RemoteExecutable;
import com.example.DynamicCode.service.procces.SavingProccesInSytemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/process")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // UWAGA: Na produkcji zastąp konkretnym adresem, np. "http://localhost:4200"
public class ProcessController {

    private final SavingProccesInSytemService processService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam("language") LanguageType language) {
        try {
            log.info("Kontroler: Odebrano plik do zapisu przez Swaggera: {}", file.getOriginalFilename());

            RemoteExecutable executable = new RemoteExecutable();
            executable.setName(file.getOriginalFilename());
            executable.setContents(file.getBytes()); // Pobieramy czyste bajty (.txt lub .exe)
            executable.setLanguage(language);

            String response = processService.save(executable);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IOException e) {
            log.error("Kontroler: Błąd podczas odczytu bajtów pliku", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Błąd przetwarzania pliku: " + e.getMessage());
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<String> saveAllExecutables(@RequestBody List<RemoteExecutable> executables) {
        return ResponseEntity.ok(processService.saveAll(executables));
    }

    @GetMapping
    public ResponseEntity<List<RemoteExecutable>> getAllExecutables() {
        return ResponseEntity.ok(processService.getAll());
    }

    // Zmieniono typ zwracany z List<RemoteExecutable> na pojedynczy obiekt RemoteExecutable
    @GetMapping("/{id}")
    public ResponseEntity<List<RemoteExecutable>> getExecutableById(@PathVariable Long id) {
        return ResponseEntity.ok(processService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateExecutable(
            @PathVariable Long id,
            @RequestBody RemoteExecutable executable) {
        return ResponseEntity.ok(processService.update(id, executable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExecutableById(@PathVariable Long id) {
        processService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<String> deleteAllExecutables() {
        return ResponseEntity.ok(processService.deleteAll());
    }
}