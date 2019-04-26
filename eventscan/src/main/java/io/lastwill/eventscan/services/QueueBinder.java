package io.lastwill.eventscan.services;

import io.lastwill.eventscan.model.Subscription;
import io.lastwill.eventscan.repositories.SubscriptionRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class QueueBinder {
    @Getter
    private final Map<String, Binding> bindings = Collections.synchronizedMap(new HashMap<>());

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private DirectExchange exchange;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @PostConstruct
    private void init() {
        subscriptionRepository.findAllSubscribed()
                .stream()
                .map(Subscription::getQueueName)
                .distinct()
                .forEach(this::add);
    }

    public void add(String queueName) {
        if (bindings.containsKey(queueName)) {
            log.warn("Attempt to add existing binding {}.", queueName);
            return;
        }

        Queue queue = new Queue(queueName);
        Binding binding = BindingBuilder.bind(queue)
                .to(exchange)
                .withQueueName();
        bindings.put(binding.getRoutingKey(), binding);
    }

    public void remove(String queueName) {
        if (!bindings.containsKey(queueName)) {
            log.error("Attempt to remove not existing binding {}.", queueName);
            return;
        }

        Binding binding = bindings.remove(queueName);
        rabbitAdmin.removeBinding(binding);
    }

    public boolean isAvailable(String queueName) {
        return bindings.containsKey(queueName);
    }
}
