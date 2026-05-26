package com.example.DynamicCode.strategy.memory;



import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.util.HashMap;
import java.util.Map;

public class MemoryJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    private final Map<String, MemoryByteCodeFile> compiledClasses = new HashMap<>();

    public MemoryJavaFileManager(JavaFileManager fileManager) {
        super(fileManager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
        MemoryByteCodeFile byteCodeFile = new MemoryByteCodeFile(className, kind);
        compiledClasses.put(className, byteCodeFile);
        return byteCodeFile;
    }

    public Map<String, byte[]> getByteCodeMap() {
        Map<String, byte[]> codeMap = new HashMap<>();
        for (Map.Entry<String, MemoryByteCodeFile> entry : compiledClasses.entrySet()) {
            codeMap.put(entry.getKey(), entry.getValue().getByteCode());
        }
        return codeMap;
    }
}