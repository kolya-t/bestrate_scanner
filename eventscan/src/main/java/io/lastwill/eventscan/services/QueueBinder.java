package io.lastwill.eventscan.services;

import io.lastwill.eventscan.model.Subscription;
import io.lastwill.eventscan.repositories.SubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class QueueBinder {
    private final ConcurrentHashMap<String, CounterBinding> bindings = new ConcurrentHashMap<>();

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
        bindings.computeIfAbsent(queueName, CounterBinding::new)
                .bind(exchange);
    }

    public void remove(String queueName) {
        CounterBinding contentBinding = bindings.get(queueName);
        if (contentBinding == null) {
            log.error("Attempt to remove not existing binding {}.", queueName);
            return;
        }
        contentBinding.unbind(rabbitAdmin);
    }

    public boolean isAvailable(String queueName) {
        return bindings.containsKey(queueName);
    }

    public static class CounterBinding {
        private final String queueName;
        private final AtomicInteger counter;
        private Binding binding;

        public CounterBinding(String queueName) {
            this.queueName = queueName;
            this.counter = new AtomicInteger(0);
        }

        public synchronized void bind(DirectExchange exchange) {
            if (counter.getAndIncrement() > 0) {
                return;
            }

            Queue queue = new Queue(queueName);
            binding = BindingBuilder.bind(queue)
                    .to(exchange)
                    .withQueueName();
        }

        public synchronized void unbind(RabbitAdmin rabbitAdmin) {
            if (counter.decrementAndGet() > 0) {
                return;
            }

            rabbitAdmin.removeBinding(binding);
        }
    }
}
