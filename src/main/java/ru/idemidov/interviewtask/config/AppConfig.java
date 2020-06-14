package ru.idemidov.interviewtask.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Value("${rabbitmq.queue.result}")
    private String resultQueueName;

    @Bean
    public MessageConverter initMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate initRabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplateWithMessageConverter = new RabbitTemplate(connectionFactory);
        rabbitTemplateWithMessageConverter.setMessageConverter(messageConverter);

        return rabbitTemplateWithMessageConverter;
    }

    @Bean
    public Queue initResultQueue() {
        return new Queue(resultQueueName);
    }
}
