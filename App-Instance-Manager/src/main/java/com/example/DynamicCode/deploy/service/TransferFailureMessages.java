package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.exception.NoFilesToTransferException;
import com.example.DynamicCode.deploy.exception.RemoteWriteException;
import com.example.DynamicCode.deploy.exception.SshConnectionException;
import com.example.DynamicCode.deploy.exception.UnsafeFileNameException;

import java.util.Map;

final class TransferFailureMessages {

    static final String SENT = "Wysłano";
    static final String FALLBACK = "Wysyłka nie powiodła się. Szczegóły są w logu aplikacji.";
    private static final Map<Class<? extends RuntimeException>, String> MESSAGES = Map.of(
            NoFilesToTransferException.class, "Brak plików do wysłania. Skompiluj projekt albo wybierz Kod.",
            UnsafeFileNameException.class, "Niedozwolona nazwa pliku. Prześlij plik ponownie pod inną nazwą.",
            SshConnectionException.class, "Nie udało się połączyć z serwerem. Sprawdź adres, login i hasło.",
            RemoteWriteException.class, "Nie udało się zapisać pliku na serwerze. Sprawdź uprawnienia do katalogu.",
            IllegalArgumentException.class, "Nie ma takiego serwera albo pliku. Odśwież listę.");

    private TransferFailureMessages() {
    }

    static String messageFor(RuntimeException failure) {
        return MESSAGES.getOrDefault(failure.getClass(), FALLBACK);
    }
}
