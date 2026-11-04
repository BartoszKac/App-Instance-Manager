package com.example.DynamicCode.inspection.controller;

import com.example.DynamicCode.inspection.dto.FilePageResponse;
import com.example.DynamicCode.inspection.dto.InspectionDetailsResponse;
import com.example.DynamicCode.inspection.dto.InspectionLoadResponse;
import com.example.DynamicCode.inspection.dto.InspectionStatusResponse;
import com.example.DynamicCode.inspection.exception.InspectionNotPossibleException;
import com.example.DynamicCode.inspection.service.InspectionQueries;
import com.example.DynamicCode.inspection.service.InspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inspections")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;
    private final InspectionQueries inspectionQueries;

    @GetMapping("/servers")
    public List<InspectionStatusResponse> listServers() {
        return inspectionQueries.listServers();
    }

    @PostMapping("/servers/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public InspectionStatusResponse start(@PathVariable Long id) {
        return inspectionService.start(id);
    }

    @GetMapping("/servers/{id}")
    public InspectionDetailsResponse details(@PathVariable Long id) {
        return inspectionQueries.details(id);
    }

    @GetMapping("/servers/{id}/files")
    public FilePageResponse children(@PathVariable Long id,
                                     @RequestParam(defaultValue = "") String parent,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "200") int size) {
        return inspectionQueries.children(id, new InspectionQueries.FilePageQuery(parent, page, size));
    }

    @GetMapping("/servers/{id}/files/search")
    public FilePageResponse search(@PathVariable Long id,
                                   @RequestParam String q,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "100") int size) {
        return inspectionQueries.search(id, new InspectionQueries.FilePageQuery(q, page, size));
    }

    @DeleteMapping("/servers/{id}/host-key")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgetHostKey(@PathVariable Long id) {
        inspectionService.forgetHostKey(id);
    }

    @GetMapping("/load")
    public InspectionLoadResponse load() {
        return new InspectionLoadResponse(inspectionService.load());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleMissingServer(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Nie ma takiego serwera. Odśwież listę.");
    }

    @ExceptionHandler(InspectionNotPossibleException.class)
    public ProblemDetail handleNotPossible(InspectionNotPossibleException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
