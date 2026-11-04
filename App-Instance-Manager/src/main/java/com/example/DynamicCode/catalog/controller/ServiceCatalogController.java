package com.example.DynamicCode.catalog.controller;

import com.example.DynamicCode.catalog.dto.ManagedServiceRequest;
import com.example.DynamicCode.catalog.dto.ManagedServiceResponse;
import com.example.DynamicCode.catalog.exception.InvalidManagedServiceException;
import com.example.DynamicCode.catalog.exception.ManagedServiceNotFoundException;
import com.example.DynamicCode.catalog.service.ServiceCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceCatalogController {

    private final ServiceCatalog serviceCatalog;

    @GetMapping
    public List<ManagedServiceResponse> list() {
        return serviceCatalog.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ManagedServiceResponse create(@RequestBody ManagedServiceRequest request) {
        return serviceCatalog.create(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        serviceCatalog.delete(id);
    }

    @ExceptionHandler(InvalidManagedServiceException.class)
    public ProblemDetail handleInvalid(InvalidManagedServiceException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(ManagedServiceNotFoundException.class)
    public ProblemDetail handleNotFound(ManagedServiceNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
