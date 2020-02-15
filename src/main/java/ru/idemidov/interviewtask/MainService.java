package ru.idemidov.interviewtask;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
public class MainService {
    private static final String FORBIDDEN_WORDS_ERROR = "Your code contains one or more of a forbidden words";

    private static final String TMP_CODE_FILE_NAME = "java_code_tmp.txt";
    private static final String TMP_CODE_PATH = "interview/";
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
     * Read temporary code (can be use for an interviewer)
     * @param username Name of a user who's code is required
     * @param rawCode Code is been typing by candidate
     */
    public void saveTmpCodeFile(final String username, final String rawCode) {
        try {
            File dir = new File(TMP_CODE_PATH, username);
            final String filePath = TMP_CODE_PATH + username;
            if (!Files.exists(Paths.get(filePath))) {
                if(!dir.mkdir()) {
                    log.error("Unable to create dir " + TMP_CODE_PATH + filePath);
                    throw new InterviewException("Sorry... Try again later!");
                }
            }
            Files.write(Paths.get(filePath, TMP_CODE_FILE_NAME), rawCode.getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read temporary code (can be use for an interviewer)
     * @param username Name of a user who's code is required
     * @param fileName Code file name (e.g. Test.java)
     * @return Code as text
     */
    public String getTmpCodeFile(final String username, final String fileName) {
        final String filePath = TMP_CODE_PATH + username;
        byte[] b = new byte[1];
        try {
            b = Files.readAllBytes(Paths.get(filePath, fileName));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return new String(b);
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

    private boolean validateCode(final String normalizedCode) {
        return Arrays.stream(normalizedCode.split("[,.;\\s]")).map(String::toUpperCase).noneMatch(Arrays.asList(forbiddenWords)::contains);
    }

    private String extractClassName(final String cleanCode) {
        Pattern regexPattern = Pattern.compile(".*class\\s(\\w+)\\s*.*");
        String className = Arrays.stream(cleanCode.split("\\n")).filter(word -> regexPattern.matcher(word).matches()).findFirst().orElse("Test");
        return regexPattern.matcher(className).replaceAll("$1");
    }
}
