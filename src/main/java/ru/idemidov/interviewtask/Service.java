package ru.idemidov.interviewtask;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Service {
    //    private static final String PATH = "./src/main/java/ru/idemidov/interviewtask/";
    private static final String TMP_CODE = "./src/main/resources";
    private static final String PATH = "./src/main/nnn/";
    private static final String CLASSPATH = "src/main/nnn";
    private static final String[] forbiddenWords = {"PROCESS", "RUNTIME", "SOCKET", "HTTP", "URL", "NET"};

    /**
     * Normalize a raw text. Replace some symbols.
     * @param dirtyCode text from a client
     * @return clean text
     */
    public static String normalizeCode(String dirtyCode) {
        String normalCode = URLDecoder.decode(dirtyCode, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder("");
        sb.append(normalCode.replace("&plus;", "+"));
//        sb.append("\n}\n}\n");
        return sb.toString();
    }

    /**
     * Re-save a code files
     * @param cleanCode Java code from a client
     * @throws IOException any troubles with files
     */
    public static void saveCodeFile(String cleanCode, String fileName) throws IOException {
        Files.deleteIfExists(Paths.get(PATH, "Test.java"));
        Files.deleteIfExists(Paths.get(PATH, "Test.class"));
        Files.deleteIfExists(Paths.get(PATH, fileName));
        Files.deleteIfExists(Paths.get(PATH, fileName));
        Files.write(Paths.get(PATH, fileName + ".java"), cleanCode.getBytes(), StandardOpenOption.CREATE);
    }

    /**
     * Compile a code and try to execute it
     * @return Exit code and (System.out or System.err)
     * @throws IOException any troubles with files
     * @throws InterruptedException subj
     * @throws InterviewException any error in client code
     */
    public static String compileAndRun(String fileName) throws IOException, InterruptedException, InterviewException {
        int runtimeExitCode;
        compile(fileName);
        Process proc = Runtime.getRuntime().exec("java -cp /home/ilya/Projects/Java/interview-task/lib/h2-1.4.200.jar:" + CLASSPATH + " " + fileName);
        runtimeExitCode = proc.waitFor();
        String result = printErrorLine(proc.getErrorStream());
        if (!result.isBlank()) {
            log.error(result);
            return "Exit code " + runtimeExitCode + "\n" + result;
        } else {
            result = printInputLine(proc.getInputStream());
            log.info(result);
            return "Exit code " + runtimeExitCode + "\n" + result;
        }
    }

    private static void compile(String fileName) throws IOException, InterruptedException {
        int compilationExitCode;
        Process proc = Runtime.getRuntime().exec("javac -cp " + CLASSPATH + " " + PATH + fileName + ".java");
        log.info(printInputLine(proc.getInputStream()));
        compilationExitCode = proc.waitFor();
        String result = printErrorLine(proc.getErrorStream());
        if (!result.isBlank()) {
            log.error(result);
            throw new InterviewException("Exit code " + compilationExitCode + "\n" + result);
        }
    }

    private static String printInputLine(InputStream is) throws IOException {
        String line;
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        return sb.toString();
    }

    private static String printErrorLine(InputStream is) throws IOException {
        String line;
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        return sb.toString();
    }

    public static boolean validateCode(String normalizedCode) {
        return Arrays.stream(normalizedCode.split("[,.;\\s]")).map(String::toUpperCase).noneMatch(Arrays.asList(forbiddenWords)::contains);
    }

    public static String extractClassName(String cleanCode) {
        Pattern regexPattern = Pattern.compile(".*class\\s(\\w+)\\s*.*");
        String className = Arrays.stream(cleanCode.split("\\n")).filter(word -> regexPattern.matcher(word).matches()).findFirst().orElse("Test");
        return regexPattern.matcher(className).replaceAll("$1");
    }

    public static void saveTmpCodeFile(String rawCode) {
        try {
            Files.write(Paths.get(TMP_CODE, "x1cv"), rawCode.getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getTmpCodeFile(String fileName) {
        byte[] b = new byte[1];
        try {
            b = Files.readAllBytes(Paths.get(TMP_CODE, fileName));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return new String(b);
    }
}

class InterviewException extends RuntimeException {
    private String message;

    public String getMessage() {
        return this.message;
    }

    public InterviewException(String message) {
        this.message = message;
    }
}
