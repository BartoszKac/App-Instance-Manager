package com.example.DynamicCode.constants.code;

public enum LanguageType {
    JAVA(".java"),
    PYTHON(".py"),
    CPP(".cpp"),
    BASH(".sh"),
    UNKNOWN("");

    private final String extension;

    LanguageType(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return this.extension;
    }
}