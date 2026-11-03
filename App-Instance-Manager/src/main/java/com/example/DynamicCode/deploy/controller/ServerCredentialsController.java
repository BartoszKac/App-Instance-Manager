package com.example.DynamicCode.deploy.controller;

import com.example.DynamicCode.deploy.dto.ServerCredentialsRequest;
import com.example.DynamicCode.deploy.exception.InvalidServerCredentialsException;
import com.example.DynamicCode.deploy.service.SerwerConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deploy/server-credentials")
@RequiredArgsConstructor
public class ServerCredentialsController {

    private final SerwerConfigurationService serwerConfigurationService;

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable Long id, @RequestBody ServerCredentialsRequest request) {
        serwerConfigurationService.updateCredentials(id, request);
    }

    @ExceptionHandler(InvalidServerCredentialsException.class)
    public ProblemDetail handleInvalid(InvalidServerCredentialsException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleMissingServer(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Nie ma takiego serwera. Odśwież listę.");
    }
}
