package io.lastwill.eventscan.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lastwill.eventscan.messages.out.BaseOutMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class MQProducer {
    @Autowired
    private RabbitTemplate rabbit;

    @Autowired
    private QueueBinder queueBinder;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${io.lastwill.eventscan.backend-mq.ttl-ms}")
    private String ttl;

    private final MessageProperties properties = new MessageProperties();

    @PostConstruct
    protected void init() {
        properties.setExpiration(ttl);
    }

    public void send(BaseOutMessage message) {
        send(message.getSubscription().getQueueName(), message);
    }

    protected synchronized void send(String queueName, BaseOutMessage notify) {
        if (!queueBinder.isAvailable(queueName)) {
            log.error("Queue '{}' is not available.", queueName);
            return;
        }

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(notify);
        } catch (JsonProcessingException e) {
            log.error("Error when converting message '{}' to string.", notify, e);
            return;
        }

        try {
            rabbit.send(queueName, new Message(bytes, properties));
            log.info("Sent message '{}' to '{}'.", new String(bytes), queueName);
        } catch (AmqpException e) {
            log.error("Error when sending message '{}' to queue '{}'.", new String(bytes), queueName, e);
        }
    }
}
