package com.example.DynamicCode.controler.test;


import com.jcraft.jsch.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

@RestController
@RequestMapping("/api/v1/files")
@CrossOrigin(origins = "*")
public class FileSshController {

    private static final String SSH_HOST = "100.103.99.6";
    private static final int SSH_PORT = 22;
    private static final String SSH_USER = "user";
    private static final String SSH_PASS = "user";
    private static final String REMOTE_DIR = "/home/username/downloads/";

    @Operation(summary = "Wysyła wybrany plik przez protokół SSH/SFTP na drugi komputer")
    @PostMapping(value = "/upload-ssh", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAndSendOverSsh(
            @Parameter(
                    description = "Plik do przesłania",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "string", format = "binary"))
            )
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Wybierz plik przed wysłaniem!");
        }

        // ... reszta kodu z JSch (identyczna jak wcześniej) ...
        return ResponseEntity.ok("Plik wysłany!");
    }
}
