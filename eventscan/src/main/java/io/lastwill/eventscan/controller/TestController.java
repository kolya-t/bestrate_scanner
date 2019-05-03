package io.lastwill.eventscan.controller;

import io.lastwill.eventscan.messages.in.SubscribeMessage;
import io.lastwill.eventscan.messages.in.UnsubscribeMessage;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.UUID;

// todo: remove
//@RestController
public class TestController {
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//
//    @Autowired
//    private DirectExchange exchange;
//
//    @Value("${io.lastwill.eventscan.mq.subscribe-queue.name}")
//    private String subscribeQueueName;
//
//    @Value("${io.lastwill.eventscan.mq.unsubscribe-queue.name}")
//    private String unsubscribeQueueName;
//
//    @PostConstruct
//    protected void init() {
//        BindingBuilder.bind(new Queue(subscribeQueueName))
//                .to(exchange)
//                .withQueueName();
//        BindingBuilder.bind(new Queue(unsubscribeQueueName))
//                .to(exchange)
//                .withQueueName();
//    }
//
//    @GetMapping("/subscribe")
//    public void subscribe() {
//        rabbitTemplate.convertAndSend(subscribeQueueName,
//                new SubscribeMessage(UUID.randomUUID(), "sdfsdfsdf", "aaaa"));
//    }
//
//    @GetMapping("/unsubscribe")
//    public void unsubscribe() {
//        rabbitTemplate.convertAndSend(unsubscribeQueueName,
//                new UnsubscribeMessage(UUID.randomUUID()));
//    }
}
