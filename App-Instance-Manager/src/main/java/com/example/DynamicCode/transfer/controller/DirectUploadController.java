package com.example.DynamicCode.transfer.controller;

import com.example.DynamicCode.transfer.dto.DirectUploadCommand;
import com.example.DynamicCode.transfer.dto.DirectUploadPageResponse;
import com.example.DynamicCode.transfer.dto.DirectUploadResponse;
import com.example.DynamicCode.transfer.exception.DirectUploadException;
import com.example.DynamicCode.transfer.service.DirectUploadRecords;
import com.example.DynamicCode.transfer.service.DirectUploadService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/transfers/servers")
@RequiredArgsConstructor
public class DirectUploadController {

    private final DirectUploadService uploadService;
    private final DirectUploadRecords records;

    @PostMapping(path = "/{id}/files", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DirectUploadResponse upload(@PathVariable Long id,
                                       @RequestParam String destination,
                                       @RequestParam String relativePath,
                                       @RequestParam(defaultValue = "false") boolean overwrite,
                                       HttpServletRequest request) throws IOException {
        DirectUploadCommand command = new DirectUploadCommand(destination, relativePath, overwrite, request.getContentLengthLong());
        return uploadService.upload(id, command, request.getInputStream());
    }

    @GetMapping("/{id}/files")
    public DirectUploadPageResponse history(@PathVariable Long id,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "50") int size) {
        return records.history(id, page, size);
    }

    @ExceptionHandler(DirectUploadException.class)
    public ProblemDetail handleUploadFailure(DirectUploadException e) {
        return ProblemDetail.forStatusAndDetail(e.getStatus(), e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleMissingServer(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Nie ma takiego serwera. Odśwież listę.");
    }
}
