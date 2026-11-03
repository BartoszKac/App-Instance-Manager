package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.dto.ServerInventoryResponse;
import com.example.DynamicCode.deploy.entity.RemoteSerwerConfiguration;
import com.example.DynamicCode.deploy.enums.InventoryScript;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerInventoryService {

    private static final Duration COMMAND_TIMEOUT = Duration.ofSeconds(45);
    private static final String AUTH_FAILED = "Serwer odrzucił login lub hasło. Popraw dane serwera.";
    private static final String CONNECTION_FAILED = "Nie udało się połączyć przez SSH. Sprawdź adres i czy serwer działa.";
    private static final String NO_DATA = "Połączono, ale serwer nie zwrócił danych.";

    private final RemoteSerwerService remoteSerwerService;
    private final SshSessionFactory sshSessionFactory;

    public ServerInventoryResponse inventory(Long id) {
        RemoteSerwerConfiguration server = remoteSerwerService.getConfigById(id);
        try {
            for (InventoryScript script : InventoryScript.orderFor(server.getOperationSystem())) {
                SshSessionFactory.SshTarget target = new SshSessionFactory.SshTarget(server.getIp(), server.getUser(), server.getPass());
                String output = sshSessionFactory.executeBounded(target, script.command(), COMMAND_TIMEOUT);
                if (InventoryOutputParser.isInventory(output)) {
                    return response(server, true, null, output);
                }
            }
            return response(server, true, NO_DATA, "");
        } catch (Exception e) {
            log.warn("Inventory of server {} failed: {}", id, e.getMessage());
            return response(server, false, toMessage(e), "");
        }
    }

    private ServerInventoryResponse response(RemoteSerwerConfiguration server, boolean connected, String error, String output) {
        return new ServerInventoryResponse(server.getIdConfiguration(), server.getName(), server.getIp(), server.getUser(),
                connected, error, output.isEmpty() ? List.of() : InventoryOutputParser.parse(output));
    }

    private String toMessage(Exception e) {
        boolean authFailed = e.getMessage() != null && e.getMessage().toLowerCase().contains("auth");
        return authFailed ? AUTH_FAILED : CONNECTION_FAILED;
    }
}
