package ru.idemidov.interviewtask.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.idemidov.interviewtask.model.Code;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
@Log4j2
public class QueueService {
    private static final String MD5 = "MD5";

    private final Main codeService;

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${redis.map}")
    private String redisMapName = "code-result";

    /**
     * Receive program code
     * @param code Program code
     */
    @RabbitListener(queues = "code")
    public void receive(Code code) {
        log.info("Received object {}", code);
        String result;
        try {
            result = codeService.compileAndRunUserCode(code.getCode());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result = e.getMessage();
        }
        saveResult(code.getUsername(), result);
    }

    private void saveResult(String username, String result) {
        String hash;
        try {
            hash = toHex(MessageDigest.getInstance(MD5).digest(username.getBytes())).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            log.error("Check your code before start!");
            hash = username;
        }
        redisTemplate.opsForHash().put(redisMapName, hash, result);
        log.info("Value {} stored in map {} with key {}", result, redisMapName, hash);
    }

    private String toHex(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }
}
