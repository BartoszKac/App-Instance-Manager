package com.example.DynamicCode.factory.code.langugage;


import com.example.DynamicCode.strategy.language.LanguageHandler;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class LanguageHandlerFactory {

    private final Map<String, LanguageHandler> handlers;

    public LanguageHandlerFactory(Map<String, LanguageHandler> handlers) {
        this.handlers = handlers;
    }


    public LanguageHandler getHandler(String extension) {
        if (extension == null || extension.isBlank()) {
            throw new IllegalArgumentException("Rozszerzenie pliku nie może być puste");
        }

        return handlers.values().stream()
                .filter(h -> h.getExtension().equalsIgnoreCase(extension))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Brak wsparcia dla rozszerzenia: " + extension));
    }
}