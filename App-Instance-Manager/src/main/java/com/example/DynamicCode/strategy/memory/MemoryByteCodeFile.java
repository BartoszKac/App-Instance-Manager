package com.example.DynamicCode.strategy.memory;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

public class MemoryByteCodeFile extends SimpleJavaFileObject {
    private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

    public MemoryByteCodeFile(String name, Kind kind) {
        super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
    }

    @Override
    public OutputStream openOutputStream() {
        return bos;
    }

    public byte[] getByteCode() {
        return bos.toByteArray();
    }
}