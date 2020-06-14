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
    private static final String TMP_CODE_PATH = "interview/guest";

    /**
     * Compile a code and try to execute it
     * @return Exit code and (System.out or System.err)
     * @throws IOException any troubles with files
     * @throws InterruptedException subj
     * @throws InterviewException any error in client code
     */
    public String compileAndRunUserCode(final String code) throws IOException, InterruptedException, InterviewException {
        int runtimeExitCode;
        String cleanCode = normalizeCode(code);
        String fileName = extractClassName(cleanCode);
        saveCodeFile(cleanCode);
        compile(fileName);
        Process proc = Runtime.getRuntime().exec(
                String.format("java -cp /home/ilya/Projects/Java/interview-task/lib/h2-1.4.200.jar:%s/ %s", TMP_CODE_PATH, fileName)
        );
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
    private void saveCodeFile(final String cleanCode) throws IOException {
        checkAndCleanCodeDirectory();
        Files.write(Paths.get(TMP_CODE_PATH, extractClassName(cleanCode) + ".java"), cleanCode.getBytes(), StandardOpenOption.CREATE);
    }

    private void checkAndCleanCodeDirectory() throws IOException {
        if (!Files.exists(Path.of(TMP_CODE_PATH))) {
            Files.createDirectories(Path.of(TMP_CODE_PATH));
        }
        File dir = new File(TMP_CODE_PATH);
        for(File file : Objects.requireNonNull(dir.listFiles())) {
            Files.deleteIfExists(Path.of(file.getAbsolutePath()));
        }
    }

    private void compile(final String fileName) throws IOException, InterruptedException {
        int compilationExitCode;
        Process proc = Runtime.getRuntime().exec(String.format("javac -cp %s %s/%s.java", TMP_CODE_PATH, TMP_CODE_PATH, fileName));
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

    private String extractClassName(final String cleanCode) {
        Pattern regexPattern = Pattern.compile(".*class\\s(\\w+)\\s*.*");
        String className = Arrays.stream(cleanCode.split("\\n"))
                .filter(word -> regexPattern.matcher(word).matches())
                .findFirst()
                .orElse("Test");
        return regexPattern.matcher(className).replaceAll("$1");
    }
}
