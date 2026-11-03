package com.example.DynamicCode.inspection.service;

import com.example.DynamicCode.deploy.entity.RemoteSerwerConfiguration;
import com.example.DynamicCode.deploy.service.RemoteSerwerService;
import com.example.DynamicCode.inspection.dto.InspectionStatusResponse;
import com.example.DynamicCode.inspection.entity.InspectionRun;
import com.example.DynamicCode.inspection.enums.InspectionState;
import com.example.DynamicCode.inspection.exception.InspectionNotPossibleException;
import com.example.DynamicCode.inspection.repository.InspectionHostKeyRepository;
import com.example.DynamicCode.inspection.repository.InspectionRunRepository;
import com.example.DynamicCode.shared.event.ServerCredentialsSavedEvent;
import com.example.DynamicCode.shared.event.ServerRemovedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InspectionServiceTest {

    private static final long SERVER_ID = 1L;

    @Mock
    private RemoteSerwerService servers;
    @Mock
    private InspectionRunRepository runs;
    @Mock
    private InspectionHostKeyRepository hostKeys;
    @Mock
    private InspectionStore store;
    @Mock
    private InspectionDispatcher dispatcher;

    private InspectionService service;

    @BeforeEach
    void setUp() {
        service = new InspectionService(servers, runs, hostKeys, store, dispatcher, new InspectionMapper());
        when(runs.findFirstByServerIdAndStateIn(any(), any())).thenReturn(Optional.empty());
        when(runs.findFirstByServerIdAndStateOrderByIdDesc(any(), any())).thenReturn(Optional.empty());
        when(runs.save(any())).thenAnswer(call -> withId(call.getArgument(0)));
    }

    @Test
    void shouldQueueRunWithoutWaitingForInspection() {
        givenServer("secret");

        InspectionStatusResponse status = service.start(SERVER_ID);

        assertThat(status.state()).isEqualTo(InspectionState.QUEUED);
        verify(dispatcher).enqueue(55L);
    }

    @Test
    void shouldNeverExposePasswordInStatus() {
        givenServer("secret");

        assertThat(service.start(SERVER_ID).toString()).doesNotContain("secret");
    }

    @Test
    void shouldReuseActiveRunInsteadOfStartingSecond() {
        givenServer("secret");
        InspectionRun active = withId(new InspectionRun(SERVER_ID, Instant.now()));
        when(runs.findFirstByServerIdAndStateIn(any(), any())).thenReturn(Optional.of(active));

        service.start(SERVER_ID);

        verify(dispatcher, never()).enqueue(any());
    }

    @Test
    void shouldRefuseServerWithoutPassword() {
        givenServer("");

        assertThatThrownBy(() -> service.start(SERVER_ID)).isInstanceOf(InspectionNotPossibleException.class);
    }

    @Test
    void shouldStartAutomaticallyWhenCredentialsAreSaved() {
        givenServer("secret");

        service.onCredentialsSaved(new ServerCredentialsSavedEvent(SERVER_ID));

        verify(dispatcher).enqueue(55L);
    }

    @Test
    void shouldNotBreakRegistrationWhenAutomaticStartFails() {
        when(servers.getConfigById(SERVER_ID)).thenThrow(new IllegalArgumentException("missing"));

        assertThatCode(() -> service.onCredentialsSaved(new ServerCredentialsSavedEvent(SERVER_ID))).doesNotThrowAnyException();
    }

    @Test
    void shouldCancelActiveRunAndRemoveDataWhenServerIsRemoved() {
        InspectionRun active = withId(new InspectionRun(SERVER_ID, Instant.now()));
        when(runs.findFirstByServerIdAndStateIn(any(), any())).thenReturn(Optional.of(active));

        service.onServerRemoved(new ServerRemovedEvent(SERVER_ID));

        verify(dispatcher).cancelRun(55L);
        verify(store).removeServer(SERVER_ID);
    }

    private void givenServer(String password) {
        when(servers.getConfigById(SERVER_ID)).thenReturn(new RemoteSerwerConfiguration(SERVER_ID, "box", "10.0.0.1", "u", password, null));
    }

    private InspectionRun withId(InspectionRun run) {
        run.setId(55L);
        return run;
    }
}
