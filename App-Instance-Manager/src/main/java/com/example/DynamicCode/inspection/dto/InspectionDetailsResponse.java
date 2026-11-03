package com.example.DynamicCode.inspection.dto;

import java.util.List;

public record InspectionDetailsResponse(InspectionStatusResponse status, List<SectionResponse> sections, long fileCount) {
}
