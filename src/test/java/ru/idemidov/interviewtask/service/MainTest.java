package ru.idemidov.interviewtask.service;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {
    public static final String USER_CODE_FILE = "test_code.java";

    @Test
    void compileAndRunUserCode_success() throws IOException, URISyntaxException, InterruptedException {
        Main mainService = new Main();
        String baseDir = System.getProperty("user.dir");
        mainService.h2DriverPath = new File(baseDir + "/src/main/resources/h2-1.4.200.jar");
        assertEquals("Exit code 0\ncappuccino\n", mainService.compileAndRunUserCode(getTestJavaCode()));
    }

    private String getTestJavaCode() throws IOException, URISyntaxException {
        return Files.readString(Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource(USER_CODE_FILE)).toURI()));
    }
}