package com.example.DynamicCode.controller.test;


import com.jcraft.jsch.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
@RequestMapping("/api/v1/files")
@CrossOrigin(origins = "*")
@Profile("dev")
public class FileSshController {

    private static final String SSH_HOST = "100.103.99.6";
    private static final int SSH_PORT = 22;
    private static final String SSH_USER = "user";
    private static final String SSH_PASS = "user";
    private static final String REMOTE_DIR = "/home/user/downloads/";

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

        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            // 1. Nawiąż sesję SSH
            JSch jsch = new JSch();
            session = jsch.getSession(SSH_USER, SSH_HOST, SSH_PORT);
            session.setPassword(SSH_PASS);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(10_000);

            // 2. Otwórz kanał SFTP
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            // 3. Upewnij się, że katalog docelowy istnieje (mkdir -p przez exec)
            ChannelExec mkdirChannel = (ChannelExec) session.openChannel("exec");
            mkdirChannel.setCommand("mkdir -p " + REMOTE_DIR);
            mkdirChannel.connect();
            Thread.sleep(300);
            mkdirChannel.disconnect();

            // 4. Wyślij plik strumieniowo (bez zapisu na dysk lokalny)
            String remoteFilePath = REMOTE_DIR + file.getOriginalFilename();
            try (InputStream inputStream = file.getInputStream()) {
                channelSftp.put(inputStream, remoteFilePath);
            }

            return ResponseEntity.ok("Plik '" + file.getOriginalFilename() + "' wysłany na " + SSH_HOST + ":" + remoteFilePath);

        } catch (JSchException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Błąd połączenia SSH: " + e.getMessage());
        } catch (SftpException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Błąd SFTP podczas wysyłania pliku: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Nieoczekiwany błąd: " + e.getMessage());
        } finally {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.exit();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }
}