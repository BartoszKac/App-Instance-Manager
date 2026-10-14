package com.example.DynamicCode.code.enums;

import java.io.File;

public final class CompileConstants {

    private static final String BASE_DIR = System.getProperty("user.dir") + File.separator + "app";

    public static String WORKING_DIR = BASE_DIR;

    private CompileConstants() {
        throw new UnsupportedOperationException("Klasa narzędziowa - nie twórz instancji!");
    }


}