package ru.idemidov.interviewtask;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.idemidov.interviewtask.model.Result;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class MainService {
    private static final String FORBIDDEN_WORDS_ERROR = "Your code contains one or more of a forbidden words";

    private static final String TMP_CODE = "./src/main/resources";
    private static final String PATH = "./src/main/nnn/";
    private static final String CLASSPATH = "src/main/nnn";
    private static final String[] forbiddenWords = {"PROCESS", "RUNTIME", "SOCKET", "HTTP", "URL", "NET"};

    /**
     * Normalize a raw text. Replace some symbols.
     * @param dirtyCode text from a client
     * @return clean text
     */
    private String normalizeCode(final String dirtyCode) {
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
    private void saveCodeFile(final String cleanCode, final String username) throws IOException {
        cleanCodeDirectory();
        Files.write(Paths.get(PATH + "/" + username, extractClassName(cleanCode) + ".java"), cleanCode.getBytes(), StandardOpenOption.CREATE);
    }

    private void cleanCodeDirectory() throws IOException {
        File dir = new File(PATH);
        for(File file : Objects.requireNonNull(dir.listFiles())) {
            Files.deleteIfExists(Path.of(file.getAbsolutePath()));
        }
    }

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

    private void compile(final String fileName) throws IOException, InterruptedException {
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

    private String printInputLine(final InputStream is) throws IOException {
        String line;
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        return sb.toString();
    }

    private String printErrorLine(final InputStream is) throws IOException {
        String line;
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        return sb.toString();
    }

    private boolean validateCode(final String normalizedCode) {
        return Arrays.stream(normalizedCode.split("[,.;\\s]")).map(String::toUpperCase).noneMatch(Arrays.asList(forbiddenWords)::contains);
    }

    private String extractClassName(final String cleanCode) {
        Pattern regexPattern = Pattern.compile(".*class\\s(\\w+)\\s*.*");
        String className = Arrays.stream(cleanCode.split("\\n")).filter(word -> regexPattern.matcher(word).matches()).findFirst().orElse("Test");
        return regexPattern.matcher(className).replaceAll("$1");
    }

    public void saveTmpCodeFile(final String rawCode) {
        try {
            Files.write(Paths.get(TMP_CODE, "x1cv"), rawCode.getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getTmpCodeFile(final String fileName) {
        byte[] b = new byte[1];
        try {
            b = Files.readAllBytes(Paths.get(TMP_CODE, fileName));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return new String(b);
    }
}
