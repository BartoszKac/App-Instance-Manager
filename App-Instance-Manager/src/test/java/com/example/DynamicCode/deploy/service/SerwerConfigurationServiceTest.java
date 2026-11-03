package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.dto.ServerCredentialsRequest;
import com.example.DynamicCode.deploy.entity.RemoteSerwerConfiguration;
import com.example.DynamicCode.deploy.enums.OperationSystem;
import com.example.DynamicCode.deploy.exception.InvalidServerCredentialsException;
import com.example.DynamicCode.shared.event.ServerCredentialsSavedEvent;
import com.example.DynamicCode.shared.event.ServerRemovedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SerwerConfigurationServiceTest {

    @Mock
    private RemoteSerwerService remoteSerwerService;
    @Mock
    private SshSessionFactory sshSessionFactory;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private SerwerConfigurationService service;

    @Test
    void shouldAnnounceCredentialsAfterRegistration() throws Exception {
        RemoteSerwerConfiguration config = server(5L, "secret");
        when(sshSessionFactory.executeCommand(any(), any(), any(), any())).thenReturn("Darwin");
        when(remoteSerwerService.updateConfig(config)).thenReturn(config);

        service.registerAndFetchDetails(config);

        assertThat(config.getOperationSystem()).isEqualTo(OperationSystem.MAC);
        verify(eventPublisher).publishEvent(new ServerCredentialsSavedEvent(5L));
    }

    @Test
    void shouldAnnounceOnlyServersWithPasswordInBulk() {
        service.registerConfigurations(List.of(server(6L, "secret"), server(7L, "")));

        verify(eventPublisher).publishEvent(new ServerCredentialsSavedEvent(6L));
        verify(eventPublisher, never()).publishEvent(new ServerCredentialsSavedEvent(7L));
    }

    @Test
    void shouldAnnounceRemoval() {
        service.removeConfiguration(8L);

        verify(eventPublisher).publishEvent(new ServerRemovedEvent(8L));
    }

    @Test
    void shouldSaveNewCredentialsAndStartInspectionAgain() {
        RemoteSerwerConfiguration config = server(9L, "old");
        when(remoteSerwerService.getConfigById(9L)).thenReturn(config);
        when(remoteSerwerService.updateConfig(config)).thenReturn(config);

        service.updateCredentials(9L, new ServerCredentialsRequest(" root ", "new"));

        assertThat(config.getUser()).isEqualTo("root");
        assertThat(config.getPass()).isEqualTo("new");
        verify(eventPublisher).publishEvent(new ServerCredentialsSavedEvent(9L));
    }

    @Test
    void shouldRejectEmptyPasswordWithoutTouchingServer() {
        assertThatThrownBy(() -> service.updateCredentials(9L, new ServerCredentialsRequest("root", "")))
                .isInstanceOf(InvalidServerCredentialsException.class);

        verify(remoteSerwerService, never()).updateConfig(any());
    }

    @Test
    void shouldNotPrintPasswordFromRequest() {
        assertThat(new ServerCredentialsRequest("root", "secret").toString()).doesNotContain("secret");
    }

    private RemoteSerwerConfiguration server(Long id, String password) {
        return new RemoteSerwerConfiguration(id, "box", "10.0.0.1", "u", password, null);
    }
}
