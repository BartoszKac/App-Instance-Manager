package com.example.DynamicCode.model;

public class CodeRequest {
    private String name;
    private String code;
    // Nowe pole określające rozszerzenie pliku (np. ".java", ".py", ".cpp")
    private String extension;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}