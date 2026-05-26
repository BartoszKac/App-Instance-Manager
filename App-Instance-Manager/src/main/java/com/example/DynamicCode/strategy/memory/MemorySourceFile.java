package com.example.DynamicCode.strategy.memory;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

public class MemorySourceFile extends SimpleJavaFileObject {

    private final String content;

    public MemorySourceFile(String className, String content) {
        super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.content = content;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return content;
    }
}