package com.example.DynamicCode.controller.test;

import com.example.DynamicCode.constants.code.LanguageType;
import com.example.DynamicCode.model.entity.procces.RemoteExecutable;
import com.example.DynamicCode.service.procces.SavingProccesInSytemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/executables")
@RequiredArgsConstructor
@Profile("dev")
public class RemoteExecutableController {

    private final SavingProccesInSytemService savingProccesInSytemService;

    /**
     * Wgrywanie pliku przez Swaggera / Multipart.
     * Dzięki consumes = MULTIPART_FORM_DATA_VALUE, Swagger wyświetli przycisk do wyboru pliku z dysku!
     */
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

            String response = savingProccesInSytemService.save(executable);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IOException e) {
            log.error("Kontroler: Błąd podczas odczytu bajtów pliku", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Błąd przetwarzania pliku: " + e.getMessage());
        }
    }

    /**
     * Aktualizacja istniejącego pliku.
     * Tutaj Swagger też podstawia przycisk wyboru nowego pliku.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateFile(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file,
            @RequestParam("language") LanguageType language) {
        try {
            log.info("Kontroler: Żądanie aktualizacji pliku o ID: {}", id);

            RemoteExecutable executable = new RemoteExecutable();
            executable.setId(id);
            executable.setName(file.getOriginalFilename());
            executable.setContents(file.getBytes());
            executable.setLanguage(language);

            String response = savingProccesInSytemService.update(id, executable);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Kontroler: Błąd podczas odczytu bajtów do aktualizacji", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Błąd aktualizacji pliku: " + e.getMessage());
        }
    }

    /**
     * Pobieranie listy plików na podstawie ID powiązanego z klasą główną.
     */
    @GetMapping("/main-class/{id}")
    public ResponseEntity<List<RemoteExecutable>> getByMainClassId(@PathVariable Long id) {
        log.info("Kontroler: Pobieranie plików dla ID klasy: {}", id);
        List<RemoteExecutable> files = savingProccesInSytemService.getById(id);

        if (files.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(files);
    }

    /**
     * Pobieranie wszystkich zapisanych plików z bazy.
     */
    @GetMapping
    public ResponseEntity<List<RemoteExecutable>> getAllFiles() {
        log.info("Kontroler: Pobieranie wszystkich plików z bazy");
        List<RemoteExecutable> files = savingProccesInSytemService.getAll();
        return ResponseEntity.ok(files);
    }

    /**
     * Usunięcie pliku po jego ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        log.info("Kontroler: Żądanie usunięcia pliku o ID: {}", id);
        savingProccesInSytemService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Całkowite wyczyszczenie tabeli z plikami.
     */
    @DeleteMapping("/all")
    public ResponseEntity<String> deleteAllFiles() {
        log.warn("Kontroler: Żądanie usunięcia wszystkich plików z bazy!");
        String response = savingProccesInSytemService.deleteAll();
        return ResponseEntity.ok(response);
    }
}