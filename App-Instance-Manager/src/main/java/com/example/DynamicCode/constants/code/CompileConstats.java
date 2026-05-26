package com.example.DynamicCode.constants.code;

import java.io.File;

public final class CompileConstats {

    private static final String BASE_DIR = System.getProperty("user.dir") + File.separator + "app";

    public static String WORKING_DIR = BASE_DIR;

    private CompileConstats() {
        throw new UnsupportedOperationException("Klasa narzędziowa - nie twórz instancji!");
    }


}