package ru.idemidov.interviewtask.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Value("${rabbitmq.queue.result}")
    private String resultQueueName;

    @Bean
    public Queue initResultQueue() {
        return new Queue(resultQueueName);
    }
}
