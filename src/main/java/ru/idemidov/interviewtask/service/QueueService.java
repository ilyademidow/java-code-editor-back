package ru.idemidov.interviewtask.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
@Log4j2
public class QueueService {
    private final Main codeService;

    private final RabbitTemplate template;
    private final Queue queue;
    @Value("${redis.map}")
    private final String redisMapName;
    @Value("${redis.url}")
    private String redisUrl;

    //TODO sudo docker run -d --hostname my-rabbit --name some-rabbit -p 5672:5672 rabbitmq:3.7-alpine
    /**
     * Receive program code
     * @param code Program code
     * @throws NoSuchAlgorithmException
     */
    @RabbitListener(queues = "code")
    public void receive(String code) throws NoSuchAlgorithmException {
        String result;
        try {
            result = codeService.compileAndRunUserCode("ilyademidow", code);
        } catch (Exception e) {
            log.error(e.getMessage());
            result = e.getMessage();
        }
        saveResult(code, result);
    }

    private void saveResult(String code, String result) {
        Config config = new Config();
        config.useSingleServer().setAddress(redisUrl);
        RedissonClient redisson = Redisson.create(config);
        RMap<String, String> map = redisson.getMap(redisMapName);
        String hash;
        try {
            hash = new String(MessageDigest.getInstance("MD5").digest(code.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            hash = String.valueOf(code.getBytes().hashCode());
        }
        map.put(hash, result);
        redisson.shutdown();
        log.info(hash + " storred");
    }
}
