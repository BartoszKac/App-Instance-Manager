package com.example.DynamicCode.controller.test;

import com.example.DynamicCode.constants.deploy.UploadStrategyType;
import com.example.DynamicCode.model.dto.deploy.TransferTask;
import com.example.DynamicCode.service.deploy.transport.EnviromentTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/deploy/transfer")
@RequiredArgsConstructor
@Tag(name = "Environment Transfer Controller", description = "Endpointy do zarządzania transferem środowisk i plików przez SSH")
@Profile("dev")
public class EnviromentTransferController {

    private final EnviromentTransferService enviromentTransferService;

    /**
     * Endpoint do masowego uruchamiania zadań transferu (Lista obiektów TransferTask).
     * Idealny, gdy chcesz wdrożyć wiele rzeczy naraz.
     */
    @PostMapping("/bulk")
    @Operation(summary = "Uruchomienie masowego transferu środowisk", description = "Przyjmuje listę zadań i przetwarza je sekwencyjnie w tle.")
    public ResponseEntity<String> initiateBulkUpload(@Valid @RequestBody List<TransferTask> transferTaskList) {
        log.info("Kontroler: Odebrano żądanie masowego transferu. Liczba zadań: {}", transferTaskList.size());

        long startTime = System.currentTimeMillis();
        enviromentTransferService.initiateUpload(transferTaskList);
        long endTime = System.currentTimeMillis();

        log.info("Kontroler: Zakończono masowy transfer w czasie {} ms.", (endTime - startTime));
        return ResponseEntity.ok("Proces masowego transferu został wykonany. Sprawdź logi systemowe w celu weryfikacji statusów poszczególnych zadań.");
    }

    /**
     * Endpoint do szybkiego uruchomienia jednego, konkretnego zadania transferu na podstawie obiektu DTO.
     */
    @PostMapping("/single-task")
    @Operation(summary = "Uruchomienie pojedynczego transferu na podstawie obiektu zadania")
    public ResponseEntity<String> initiateSingleTask(@Valid @RequestBody TransferTask transferTask) {
        log.info("Kontroler: Odebrano żądanie pojedynczego transferu dla MainClassId: {}", transferTask.mainClassId());

        try {
            enviromentTransferService.initiateUpload(List.of(transferTask));
            return ResponseEntity.ok("Zadanie transferu zostało przetworzone.");
        } catch (Exception e) {
            log.error("Kontroler: Wyjątek podczas realizacji zadania transferu: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Wystąpił błąd podczas transferu: " + e.getMessage());
        }
    }

    /**
     * Endpoint typu GET/POST z parametrami query (URL), pozwalający na odpalenie transferu bezpośrednio poprzez przekazanie ID w adresie.
     * Bardzo wygodne do testowania bezpośrednio w przeglądarce lub uproszczonym wywołaniu w Swaggerze.
     */
    @PostMapping("/direct")
    @Operation(summary = "Bezpośrednie wywołanie transferu za pomocą parametrów URL", description = "Pozwala na pominięcie przesyłania struktury JSON.")
    public ResponseEntity<String> transferEnvironmentDirectly(
            @RequestParam("mainClassId") Long mainClassId,
            @RequestParam("configurationId") Long configurationId,
            @RequestParam("uploadStrategyType") UploadStrategyType uploadStrategyType) {

        log.info("Kontroler: Wywołanie bezpośrednie transferu (MainClass: {}, Config: {}, Strategia: {})",
                mainClassId, configurationId, uploadStrategyType);

        try {
            long startTime = System.currentTimeMillis();
            enviromentTransferService.transferEnvironment(mainClassId, configurationId, uploadStrategyType);
            long endTime = System.currentTimeMillis();

            return ResponseEntity.ok("Środowisko zostało pomyślnie przetransferowane przez SSH w czasie " + (endTime - startTime) + " ms.");
        } catch (Exception e) {
            log.error("Kontroler: Błąd podczas bezpośredniego transferu środowiska. Powód: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Nie udało się przeprowadzić transferu: " + e.getMessage());
        }
    }
}