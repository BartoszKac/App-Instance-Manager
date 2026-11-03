package com.example.DynamicCode.inspection.service;

import com.example.DynamicCode.deploy.entity.RemoteSerwerConfiguration;
import com.example.DynamicCode.deploy.service.RemoteSerwerService;
import com.example.DynamicCode.inspection.dto.FilePageResponse;
import com.example.DynamicCode.inspection.dto.InspectionDetailsResponse;
import com.example.DynamicCode.inspection.dto.InspectionStatusResponse;
import com.example.DynamicCode.inspection.dto.SectionResponse;
import com.example.DynamicCode.inspection.entity.InspectedFile;
import com.example.DynamicCode.inspection.entity.InspectionRun;
import com.example.DynamicCode.inspection.enums.InspectionState;
import com.example.DynamicCode.inspection.repository.InspectedFileRepository;
import com.example.DynamicCode.inspection.repository.InspectionRunRepository;
import com.example.DynamicCode.inspection.repository.InspectionSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InspectionQueries {

    private static final int MAX_PAGE_SIZE = 500;

    private final RemoteSerwerService servers;
    private final InspectionRunRepository runs;
    private final InspectionSectionRepository sections;
    private final InspectedFileRepository files;
    private final InspectionMapper mapper;

    public List<InspectionStatusResponse> listServers() {
        Map<Long, InspectionRun> latest = byServer(runs.findLatestPerServer());
        Map<Long, InspectionRun> done = byServer(runs.findLatestDonePerServer());
        return servers.getAllConfigs().stream()
                .map(server -> mapper.toStatus(server, latest.get(server.getIdConfiguration()), done.get(server.getIdConfiguration())))
                .toList();
    }

    public InspectionDetailsResponse details(Long serverId) {
        RemoteSerwerConfiguration server = servers.getConfigById(serverId);
        Optional<InspectionRun> lastDone = lastDone(serverId);
        InspectionStatusResponse status = mapper.toStatus(server, runs.findFirstByServerIdOrderByIdDesc(serverId).orElse(null), lastDone.orElse(null));
        List<SectionResponse> found = lastDone
                .map(run -> sections.findByRunIdOrderById(run.getId()).stream().map(mapper::toSection).toList())
                .orElse(List.of());
        return new InspectionDetailsResponse(status, found, lastDone.map(InspectionRun::getFileCount).orElse(0L));
    }

    public FilePageResponse children(Long serverId, FilePageQuery query) {
        servers.getConfigById(serverId);
        return lastDone(serverId)
                .map(run -> toPage(files.findChildren(run.getId(), query.text(), pageable(query))))
                .orElse(FilePageResponse.EMPTY);
    }

    public FilePageResponse search(Long serverId, FilePageQuery query) {
        servers.getConfigById(serverId);
        String pattern = "%" + escapeLike(query.text().toLowerCase(Locale.ROOT)) + "%";
        return lastDone(serverId)
                .map(run -> toPage(files.search(run.getId(), pattern, pageable(query))))
                .orElse(FilePageResponse.EMPTY);
    }

    private Optional<InspectionRun> lastDone(Long serverId) {
        return runs.findFirstByServerIdAndStateOrderByIdDesc(serverId, InspectionState.DONE);
    }

    private Map<Long, InspectionRun> byServer(List<InspectionRun> found) {
        return found.stream().collect(Collectors.toMap(InspectionRun::getServerId, Function.identity()));
    }

    private PageRequest pageable(FilePageQuery query) {
        return PageRequest.of(Math.max(query.page(), 0), Math.min(Math.max(query.size(), 1), MAX_PAGE_SIZE));
    }

    private FilePageResponse toPage(Page<InspectedFile> page) {
        return new FilePageResponse(page.getContent().stream().map(mapper::toFile).toList(), page.getTotalElements());
    }

    private String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    public record FilePageQuery(String text, int page, int size) {
    }
}
