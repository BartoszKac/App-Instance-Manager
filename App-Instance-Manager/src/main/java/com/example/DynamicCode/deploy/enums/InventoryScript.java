package com.example.DynamicCode.deploy.enums;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.function.UnaryOperator;

public enum InventoryScript {
    POSIX("inventory/posix.sh", script -> script),
    WINDOWS("inventory/windows.ps1", InventoryScript::toEncodedPowerShell);

    private final String resource;
    private final UnaryOperator<String> toCommand;

    InventoryScript(String resource, UnaryOperator<String> toCommand) {
        this.resource = resource;
        this.toCommand = toCommand;
    }

    public static List<InventoryScript> orderFor(OperationSystem operationSystem) {
        return operationSystem == OperationSystem.WINDOWS ? List.of(WINDOWS, POSIX) : List.of(POSIX, WINDOWS);
    }

    public String command() {
        try {
            String script = new ClassPathResource(resource).getContentAsString(StandardCharsets.UTF_8);
            return toCommand.apply(script);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String toEncodedPowerShell(String script) {
        String encoded = Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
        return "powershell -NoProfile -NonInteractive -EncodedCommand " + encoded;
    }
}
