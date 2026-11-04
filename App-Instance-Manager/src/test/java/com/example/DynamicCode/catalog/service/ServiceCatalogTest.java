package com.example.DynamicCode.catalog.service;

import com.example.DynamicCode.catalog.dto.ManagedServiceRequest;
import com.example.DynamicCode.catalog.dto.ManagedServiceResponse;
import com.example.DynamicCode.catalog.entity.ManagedService;
import com.example.DynamicCode.catalog.exception.InvalidManagedServiceException;
import com.example.DynamicCode.catalog.exception.ManagedServiceNotFoundException;
import com.example.DynamicCode.catalog.repository.ManagedServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceCatalogTest {

    @Mock
    private ManagedServiceRepository repository;

    @InjectMocks
    private ServiceCatalog catalog;

    @Test
    void shouldTrimFieldsAndSaveWhenRequestIsValid() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ManagedServiceResponse response = catalog.create(new ManagedServiceRequest(" Wiki ", " 192.168.1.5 ", 8080, "  "));

        ArgumentCaptor<ManagedService> saved = ArgumentCaptor.forClass(ManagedService.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getName()).isEqualTo("Wiki");
        assertThat(saved.getValue().getDescription()).isNull();
        assertThat(response.host()).isEqualTo("192.168.1.5");
        assertThat(response.port()).isEqualTo(8080);
    }

    @ParameterizedTest
    @CsvSource(nullValues = "NULL", value = {
            "NULL, 192.168.1.5, 80",
            "'  ', 192.168.1.5, 80",
            "Wiki, NULL, 80",
            "Wiki, 'host;rm -rf', 80",
            "Wiki, -oN, 80",
            "Wiki, 192.168.1.5, NULL",
            "Wiki, 192.168.1.5, 0",
            "Wiki, 192.168.1.5, 65536"
    })
    void shouldRejectInvalidRequestWithoutSaving(String name, String host, Integer port) {
        ManagedServiceRequest request = new ManagedServiceRequest(name, host, port, null);

        assertThatThrownBy(() -> catalog.create(request)).isInstanceOf(InvalidManagedServiceException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectTooLongDescription() {
        ManagedServiceRequest request = new ManagedServiceRequest("Wiki", "wiki.lan", 80, "x".repeat(256));

        assertThatThrownBy(() -> catalog.create(request)).isInstanceOf(InvalidManagedServiceException.class);
    }

    @Test
    void shouldListServicesAsDtos() {
        when(repository.findAll()).thenReturn(List.of(new ManagedService(1L, "Wiki", "wiki.lan", 80, null)));

        assertThat(catalog.list()).containsExactly(new ManagedServiceResponse(1L, "Wiki", "wiki.lan", 80, null));
    }

    @Test
    void shouldThrowWhenDeletingMissingService() {
        when(repository.existsById(5L)).thenReturn(false);

        assertThatThrownBy(() -> catalog.delete(5L)).isInstanceOf(ManagedServiceNotFoundException.class);
    }

    @Test
    void shouldDeleteExistingService() {
        when(repository.existsById(5L)).thenReturn(true);

        catalog.delete(5L);

        verify(repository).deleteById(5L);
    }
}
