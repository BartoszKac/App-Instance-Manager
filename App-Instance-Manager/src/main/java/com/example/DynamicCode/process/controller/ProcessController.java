package com.example.DynamicCode.process.controller;

import com.example.DynamicCode.code.enums.LanguageType;
import com.example.DynamicCode.process.entity.RemoteExecutable;
import com.example.DynamicCode.process.exception.InvalidExecutableException;
import com.example.DynamicCode.process.service.SavingProcessInSystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/process")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProcessController {

    private final SavingProcessInSystemService processService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam("language") LanguageType language,
            @RequestParam(value = "description", required = false) String description) throws IOException {
        RemoteExecutable executable = new RemoteExecutable();
        executable.setName(file.getOriginalFilename());
        executable.setContents(file.getBytes());
        executable.setLanguage(language);
        executable.setDescription(description == null || description.isBlank() ? null : description.strip());
        return ResponseEntity.status(HttpStatus.CREATED).body(processService.save(executable));
    }

    @PostMapping("/bulk")
    public ResponseEntity<String> saveAllExecutables(@RequestBody List<RemoteExecutable> executables) {
        return ResponseEntity.ok(processService.saveAll(executables));
    }

    @GetMapping
    public ResponseEntity<List<RemoteExecutable>> getAllExecutables() {
        return ResponseEntity.ok(processService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<RemoteExecutable>> getExecutableById(@PathVariable Long id) {
        return ResponseEntity.ok(processService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateExecutable(@PathVariable Long id, @RequestBody RemoteExecutable executable) {
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

    @ExceptionHandler(InvalidExecutableException.class)
    public ProblemDetail handleInvalid(InvalidExecutableException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public ProblemDetail handleUnreadable(IOException e) {
        log.warn("Uploaded file could not be read", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Nie udało się odczytać pliku. Spróbuj ponownie.");
    }
}
