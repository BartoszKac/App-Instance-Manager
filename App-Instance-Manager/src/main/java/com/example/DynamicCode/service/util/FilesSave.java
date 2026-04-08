package com.example.DynamicCode.service.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FilesSave {

    List<String> lines = new ArrayList<>();

    public void writeString( String content) {
        lines.add(content);
    }
    public void deleteString(String content) {
        lines.remove(content);
    }

        @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
