package com.example.DynamicCode.process.service;

final class ExecutableFileNameRule {

    private ExecutableFileNameRule() {
    }

    static boolean isSafe(String name) {
        if (name == null || name.isBlank() || name.equals(".") || name.equals("..")) {
            return false;
        }
        return !name.startsWith("-") && name.chars().noneMatch(ExecutableFileNameRule::isForbidden);
    }

    private static boolean isForbidden(int character) {
        return character == '/' || character == '\\' || Character.isISOControl(character);
    }
}
