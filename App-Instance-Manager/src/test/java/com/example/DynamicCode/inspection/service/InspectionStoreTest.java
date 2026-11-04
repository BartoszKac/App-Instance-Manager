package com.example.DynamicCode.inspection.service;

import com.example.DynamicCode.inspection.entity.InspectedFile;
import com.example.DynamicCode.inspection.entity.InspectionRun;
import com.example.DynamicCode.inspection.enums.InspectionFailureKind;
import com.example.DynamicCode.inspection.enums.InspectionState;
import com.example.DynamicCode.inspection.repository.InspectedFileRepository;
import com.example.DynamicCode.inspection.repository.InspectionHostKeyRepository;
import com.example.DynamicCode.inspection.repository.InspectionRunRepository;
import com.example.DynamicCode.inspection.repository.InspectionSectionRepository;
import com.example.inspection.grpc.v1.Completed;
import com.example.inspection.grpc.v1.FileBatch;
import com.example.inspection.grpc.v1.FileEntry;
import com.example.inspection.grpc.v1.InspectEvent;
import com.example.inspection.grpc.v1.Section;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(InspectionStore.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class InspectionStoreTest {

    @Autowired
    private InspectionStore store;
    @Autowired
    private InspectionRunRepository runs;
    @Autowired
    private InspectedFileRepository files;
    @Autowired
    private InspectionSectionRepository sections;
    @Autowired
    private InspectionHostKeyRepository hostKeys;

    @Test
    void shouldStoreFilesAndCompleteRun() {
        Long runId = newRun(100L);
        store.apply(runId, filesEvent("/", "/etc", "/etc/ssh", "/etc/a%b"));
        store.apply(runId, section("hostname"));

        store.apply(runId, completed("SHA256:k", List.of("/etc/ssh")));

        InspectionRun run = runs.findById(runId).orElseThrow();
        assertThat(run.getState()).isEqualTo(InspectionState.DONE);
        assertThat(run.getFileCount()).isEqualTo(4);
        assertThat(hostKeys.findById(100L).orElseThrow().getFingerprint()).isEqualTo("SHA256:k");
        assertThat(hostKeys.findById(100L).orElseThrow().getIp()).isEqualTo("10.0.0.1");
        List<InspectedFile> children = files.findChildren(runId, "/etc", PageRequest.of(0, 10)).getContent();
        assertThat(children).extracting(InspectedFile::getPath).containsExactly("/etc/ssh", "/etc/a%b");
        assertThat(children.get(0).isDenied()).isTrue();
        assertThat(sections.findByRunIdOrderById(runId)).hasSize(1);
    }

    @Test
    void shouldTreatLikeWildcardsAsPlainTextWhenSearching() {
        Long runId = newRun(101L);
        store.apply(runId, filesEvent("/ab", "/a%b"));

        assertThat(files.search(runId, "%a\\%b%", PageRequest.of(0, 10)).getContent())
                .extracting(InspectedFile::getPath).containsExactly("/a%b");
    }

    @Test
    void shouldRemoveOlderRunsOnlyAfterNewOneCompletes() {
        Long oldRun = newRun(102L);
        store.apply(oldRun, filesEvent("/old"));
        store.apply(oldRun, completed("SHA256:k", List.of()));
        Long newRun = newRun(102L);
        store.apply(newRun, filesEvent("/new"));

        assertThat(files.findChildren(oldRun, "/", PageRequest.of(0, 10)).getContent()).hasSize(1);

        store.apply(newRun, completed("SHA256:k", List.of()));

        assertThat(runs.findById(oldRun)).isEmpty();
        assertThat(files.findChildren(oldRun, "/", PageRequest.of(0, 10)).getContent()).isEmpty();
        assertThat(files.findChildren(newRun, "/", PageRequest.of(0, 10)).getContent()).hasSize(1);
    }

    @Test
    void shouldDropPartialDataWhenRunFailsOrIsRequeued() {
        Long failing = newRun(103L);
        store.apply(failing, filesEvent("/x"));
        store.fail(failing, "błąd", InspectionFailureKind.OTHER);
        assertThat(files.findChildren(failing, "/", PageRequest.of(0, 10)).getContent()).isEmpty();
        assertThat(runs.findById(failing).orElseThrow().getState()).isEqualTo(InspectionState.FAILED);

        Long requeued = newRun(104L);
        store.apply(requeued, filesEvent("/y"));
        store.requeue(requeued);
        assertThat(files.findChildren(requeued, "/", PageRequest.of(0, 10)).getContent()).isEmpty();
        assertThat(runs.findById(requeued).orElseThrow().getState()).isEqualTo(InspectionState.QUEUED);
    }

    @Test
    void shouldFailOnlyInterruptedRunsAfterRestart() {
        Long running = newRun(105L);
        Long done = newRun(106L);
        store.apply(done, completed("", List.of()));

        store.failInterrupted("restart");

        assertThat(runs.findById(running).orElseThrow().getState()).isEqualTo(InspectionState.FAILED);
        assertThat(runs.findById(done).orElseThrow().getState()).isEqualTo(InspectionState.DONE);
    }

    @Test
    void shouldRemoveAllDataOfServer() {
        Long runId = newRun(107L);
        store.apply(runId, filesEvent("/z"));
        store.apply(runId, completed("SHA256:k", List.of()));

        store.removeServer(107L);

        assertThat(runs.findById(runId)).isEmpty();
        assertThat(hostKeys.findById(107L)).isEmpty();
        assertThat(files.findChildren(runId, "/", PageRequest.of(0, 10)).getContent()).isEmpty();
    }

    private Long newRun(Long serverId) {
        Long id = runs.save(new InspectionRun(serverId, Instant.now())).getId();
        store.markRunning(id, "10.0.0.1");
        return id;
    }

    private InspectEvent filesEvent(String... paths) {
        FileBatch.Builder batch = FileBatch.newBuilder();
        for (String path : paths) {
            int last = path.lastIndexOf('/');
            String parent = path.equals("/") ? "" : (last == 0 ? "/" : path.substring(0, last));
            batch.addEntries(FileEntry.newBuilder().setPath(path).setParent(parent).setName(path.substring(last + 1))
                    .setType(path.equals("/etc/ssh") ? "d" : "f"));
        }
        return InspectEvent.newBuilder().setFiles(batch).build();
    }

    private InspectEvent section(String key) {
        return InspectEvent.newBuilder().setSection(Section.newBuilder().setKey(key).setLabel("l").setCategory("c").setValue("v")).build();
    }

    private InspectEvent completed(String hostKey, List<String> denied) {
        return InspectEvent.newBuilder().setCompleted(Completed.newBuilder().setHostKey(hostKey).addAllDeniedPaths(denied)).build();
    }
}
