package ru.idemidov.interviewtask;

import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.idemidov.interviewtask.model.Code;
import ru.idemidov.interviewtask.model.Result;

import javax.websocket.server.PathParam;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@RestController
@RequestMapping(value = "api/v1", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class Controller {
    private static final String FORBIDDEN_WORDS_ERROR = "Your code contains one or more of a forbidden words";
    @PostMapping("run")
    public ResponseEntity<Result> execute(@RequestBody Code code) {
        log.info(("Invoked " + code));
        try {
            String cleanCode = Service.normalizeCode(code.getCode());
            String className = Service.extractClassName(cleanCode);
            if (!Service.validateCode(cleanCode)) {
                return ResponseEntity.ok(new Result(null, FORBIDDEN_WORDS_ERROR));
            }
            Service.saveCodeFile(cleanCode, className);
            try {
                return ResponseEntity.ok(new Result(Service.compileAndRun(className), null));
            } catch (InterviewException e) {
                return ResponseEntity.ok(new Result(null, e.getMessage()));
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(new Result("", null));
    }

    @PostMapping("save_tmp")
    public ResponseEntity<String> saveTmp(@RequestBody Code code) {
        Service.saveTmpCodeFile(code.getCode());
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "read_tmp/{fileName}")
    public ResponseEntity<Result> readTmp(@RequestBody Code code) {
        return ResponseEntity.ok(new Result(Service.getTmpCodeFile(code.getCode()), ""));
    }
}
