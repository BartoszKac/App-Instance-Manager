package com.example.DynamicCode.controller.process;

import com.example.DynamicCode.model.entity.procces.RemoteExecutable;
import com.example.DynamicCode.service.procces.SavingProccesInSytemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/process")
@RequiredArgsConstructor
public class ProcessController {

    private final SavingProccesInSytemService processService;

    @PostMapping
    public ResponseEntity<String> saveExecutable(@RequestBody RemoteExecutable executable) {
        return ResponseEntity.ok(processService.save(executable));
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
    public ResponseEntity<List<RemoteExecutable>> getExecutablesById(@PathVariable Long id) {
        return ResponseEntity.ok(processService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateExecutable(
            @PathVariable Long id,
            @RequestBody RemoteExecutable executable) {
        // Przekazujemy ID, choć w obecnym serwisie metoda update ignoruje podane ID i opiera się na obiekcie
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