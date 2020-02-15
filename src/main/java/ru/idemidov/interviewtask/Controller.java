package ru.idemidov.interviewtask;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.idemidov.interviewtask.model.Code;
import ru.idemidov.interviewtask.model.Result;

import java.io.IOException;

@RestController
@RequestMapping(value = "api/v1", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class Controller {
    private final MainService service;

    @Autowired
    public Controller(final MainService service) {
        this.service = service;
    }

    @PostMapping("run")
    public ResponseEntity<Result> execute(@RequestBody Code code) {
        log.info(("Invoked " + code));
        try {
            if (code.getUsername().isBlank()) {
                return ResponseEntity.badRequest().build();
            }
            try {
                return ResponseEntity.ok(new Result(service.compileAndRunUserCode(code.getUsername(), code.getCode()), null));
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
        log.info("save_tmp invoked");
        service.saveTmpCodeFile(code.getUsername(), code.getCode());
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "read_tmp/{fileName}")
    public ResponseEntity<Result> readTmp(@RequestBody Code code) {
        log.info("read_tmp invoked");
        return ResponseEntity.ok(new Result(service.getTmpCodeFile(code.getUsername(), code.getCode()), ""));
    }
}
