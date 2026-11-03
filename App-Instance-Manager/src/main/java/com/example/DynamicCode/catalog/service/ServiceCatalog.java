package com.example.DynamicCode.catalog.service;

import com.example.DynamicCode.catalog.dto.ManagedServiceRequest;
import com.example.DynamicCode.catalog.dto.ManagedServiceResponse;
import com.example.DynamicCode.catalog.entity.ManagedService;
import com.example.DynamicCode.catalog.exception.InvalidManagedServiceException;
import com.example.DynamicCode.catalog.exception.ManagedServiceNotFoundException;
import com.example.DynamicCode.catalog.repository.ManagedServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ServiceCatalog {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 255;
    private static final Pattern HOST = Pattern.compile("[A-Za-z0-9][A-Za-z0-9.-]{0,252}");

    private final ManagedServiceRepository repository;

    public List<ManagedServiceResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public ManagedServiceResponse create(ManagedServiceRequest request) {
        validate(request);
        ManagedService service = new ManagedService(null, request.name().trim(), request.host().trim(),
                request.port(), blankToNull(request.description()));
        return toResponse(repository.save(service));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ManagedServiceNotFoundException();
        }
        repository.deleteById(id);
    }

    private void validate(ManagedServiceRequest request) {
        if (request.name() == null || request.name().isBlank() || request.name().length() > MAX_NAME_LENGTH) {
            throw new InvalidManagedServiceException("Podaj nazwę serwisu, maksymalnie 100 znaków.");
        }
        if (request.host() == null || !HOST.matcher(request.host().trim()).matches()) {
            throw new InvalidManagedServiceException("Podaj poprawny adres IP lub nazwę hosta.");
        }
        if (request.port() == null || request.port() < 1 || request.port() > 65535) {
            throw new InvalidManagedServiceException("Port musi być liczbą od 1 do 65535.");
        }
        if (request.description() != null && request.description().length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidManagedServiceException("Opis może mieć maksymalnie 255 znaków.");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ManagedServiceResponse toResponse(ManagedService service) {
        return new ManagedServiceResponse(service.getId(), service.getName(), service.getHost(),
                service.getPort(), service.getDescription());
    }
}
