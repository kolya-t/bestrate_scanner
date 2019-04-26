package io.lastwill.eventscan.services;

import io.lastwill.eventscan.messages.out.BaseOutMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MQProducer {
    @Autowired
    private RabbitTemplate rabbit;

    @Autowired
    private QueueBinder queueBinder;

    public void send(BaseOutMessage message) {
        send(message.getSubscription().getQueueName(), message);
    }

    protected synchronized void send(String queueName, BaseOutMessage notify) {
        if (!queueBinder.isAvailable(queueName)) {
            log.error("Queue {} is not available.", queueName);
            return;
        }

        try {
            rabbit.convertAndSend(queueName, notify);
        } catch (AmqpException e) {
            log.error("Error when sending message {} to queue {}.", notify, queueName, e);
        }
    }
}
