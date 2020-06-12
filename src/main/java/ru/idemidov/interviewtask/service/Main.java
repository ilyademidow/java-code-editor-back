package ru.idemidov.interviewtask.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.idemidov.interviewtask.InterviewException;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
@Service
public class Main {
    private static final String TMP_CODE_PATH = "interview/";

    private static final String FORBIDDEN_WORDS_ERROR = "Your code contains one or more of a forbidden words";
    private static final String[] forbiddenWords = {"PROCESS", "RUNTIME", "SOCKET", "HTTP", "URL", "NET"};

    /**
     * Compile a code and try to execute it
     * @return Exit code and (System.out or System.err)
     * @throws IOException any troubles with files
     * @throws InterruptedException subj
     * @throws InterviewException any error in client code
     */
    public String compileAndRunUserCode(final String username, final String code) throws IOException, InterruptedException, InterviewException {
        int runtimeExitCode;
        String cleanCode = normalizeCode(code);
        if (!validateCode(cleanCode)) {
            throw new InterviewException(FORBIDDEN_WORDS_ERROR);
        }
        String fileName = extractClassName(cleanCode);
        saveCodeFile(cleanCode, username);
        compile(username, fileName);
        final String filePath = TMP_CODE_PATH + username;
        Process proc = Runtime.getRuntime().exec("java -cp /home/ilya/Projects/Java/interview-task/lib/h2-1.4.200.jar:" + filePath + "/ " + fileName);
        runtimeExitCode = proc.waitFor();
        String result = printErrorLine(proc.getErrorStream());
        if (!result.isBlank()) {
            log.error(result);
        } else {
            result = printInputLine(proc.getInputStream());
            log.info(result);
        }
        return "Exit code " + runtimeExitCode + "\n" + result;
    }

    /**
     * Normalize a raw text. Replace some symbols.
     * @param dirtyCode text from a client
     * @return clean text
     */
    private String normalizeCode(final String dirtyCode) {
        String normalCode = URLDecoder.decode(dirtyCode, StandardCharsets.UTF_8);
        return "" + normalCode.replace("&plus;", "+");
    }

    /**
     * Re-save a code files
     * @param cleanCode Java code from a client
     * @throws IOException any troubles with files
     */
    private void saveCodeFile(final String cleanCode, final String username) throws IOException {
        final String filePath = TMP_CODE_PATH + username;
        cleanCodeDirectory(username);
        Files.write(Paths.get(filePath, extractClassName(cleanCode) + ".java"), cleanCode.getBytes(), StandardOpenOption.CREATE);
    }

    private void cleanCodeDirectory(final String username) throws IOException {
        File dir = new File(TMP_CODE_PATH + username);
        for(File file : Objects.requireNonNull(dir.listFiles())) {
            Files.deleteIfExists(Path.of(file.getAbsolutePath()));
        }
    }

    private void compile(final String username, final String fileName) throws IOException, InterruptedException {
        int compilationExitCode;
        final String filePath = TMP_CODE_PATH + username;
        Process proc = Runtime.getRuntime().exec("javac -cp " + TMP_CODE_PATH + " " + filePath + "/" + fileName + ".java");
        log.info(printInputLine(proc.getInputStream()));
        compilationExitCode = proc.waitFor();
        String result = printErrorLine(proc.getErrorStream());
        if (!result.isBlank()) {
            log.error(result);
            throw new InterviewException("Exit code " + compilationExitCode + "\n" + result);
        }
    }

    private String printInputLine(final InputStream is) throws IOException {
        String line;
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    private String printErrorLine(final InputStream is) throws IOException {
        String line;
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    @Deprecated
    private boolean validateCode(final String normalizedCode) {
        return Arrays.stream(normalizedCode.split("[,.;\\s]")).map(String::toUpperCase).noneMatch(Arrays.asList(forbiddenWords)::contains);
    }

    private String extractClassName(final String cleanCode) {
        Pattern regexPattern = Pattern.compile(".*class\\s(\\w+)\\s*.*");
        String className = Arrays.stream(cleanCode.split("\\n")).filter(word -> regexPattern.matcher(word).matches()).findFirst().orElse("Test");
        return regexPattern.matcher(className).replaceAll("$1");
    }
}
