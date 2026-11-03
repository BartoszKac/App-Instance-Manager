package com.example.DynamicCode.inspection.service;

import com.example.DynamicCode.deploy.entity.RemoteSerwerConfiguration;
import com.example.DynamicCode.inspection.dto.FileEntryResponse;
import com.example.DynamicCode.inspection.dto.InspectionStatusResponse;
import com.example.DynamicCode.inspection.dto.SectionResponse;
import com.example.DynamicCode.inspection.entity.InspectedFile;
import com.example.DynamicCode.inspection.entity.InspectionRun;
import com.example.DynamicCode.inspection.entity.InspectionSection;
import org.springframework.stereotype.Component;

@Component
public class InspectionMapper {

    public InspectionStatusResponse toStatus(RemoteSerwerConfiguration server, InspectionRun latest, InspectionRun lastDone) {
        return new InspectionStatusResponse(
                server.getIdConfiguration(), server.getName(), server.getIp(), server.getUser(),
                latest == null ? null : latest.getState(),
                latest == null ? null : latest.getStage(),
                latest == null ? 0 : latest.getFileCount(),
                latest == null ? null : latest.getError(),
                latest == null ? null : latest.getFailureKind(),
                lastDone == null ? null : lastDone.getFinishedAt(),
                lastDone != null && lastDone.isTruncated());
    }

    public SectionResponse toSection(InspectionSection section) {
        return new SectionResponse(section.getKey(), section.getLabel(), section.getCategory(), section.getValue());
    }

    public FileEntryResponse toFile(InspectedFile file) {
        return new FileEntryResponse(file.getPath(), file.getName(), file.getType(), file.getSizeBytes(),
                file.getOwner(), file.getGroup(), file.getMode(), file.getModifiedEpochSeconds(), file.isDenied());
    }
}
