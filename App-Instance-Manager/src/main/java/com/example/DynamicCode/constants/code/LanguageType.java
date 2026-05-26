package com.example.DynamicCode.constants.code;

public enum LanguageType {
    JAVA("java"),
    PYTHON("py"),
    CPP("cpp"),
    BASH("sh");

    private final String extension;

    // Konstruktor enuma przypisujący rozszerzenie do każdego języka
    LanguageType(String extension) {
        this.extension = extension;
    }

    // Metoda zwracająca rozszerzenie pliku
    public String getExtension() {
        return this.extension;
    }
}