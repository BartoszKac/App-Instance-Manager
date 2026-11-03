package com.example.DynamicCode.deploy.controller;

import com.example.DynamicCode.deploy.dto.ServerInventoryResponse;
import com.example.DynamicCode.deploy.service.ServerInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deploy/server-inventory")
@RequiredArgsConstructor
public class ServerInventoryController {

    private final ServerInventoryService serverInventoryService;

    @GetMapping("/{id}")
    public ServerInventoryResponse inventory(@PathVariable Long id) {
        return serverInventoryService.inventory(id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleMissingServer(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Nie ma takiego serwera. Odśwież listę.");
    }
}
