package io.lastwill.eventscan.services;

import io.lastwill.eventscan.messages.in.SubscribeMessage;
import io.lastwill.eventscan.messages.in.UnsubscribeMessage;
import io.lastwill.eventscan.model.Subscription;
import io.lastwill.eventscan.repositories.SubscriptionRepository;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class MQConsumerSubscription {
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private QueueBinder queueBinder;

    @RabbitListener(queues = "${io.lastwill.eventscan.mq.subscribe-queue.name}")
    public void subscribe(SubscribeMessage message) {
        subscribe(message.getId(), message.getTcrAddress(), message.getQueueName());
    }

    @RabbitListener(queues = "${io.lastwill.eventscan.mq.unsubscribe-queue.name}")
    public void unsubscribe(UnsubscribeMessage message) {
        unsubscribe(message.getId());
    }

    @Synchronized
    protected void subscribe(UUID id, String tcrAddress, String queueName) {
        Subscription subscription = subscriptionRepository.findByClientId(id);
        if (subscription == null) {
            subscription = new Subscription(id, tcrAddress, queueName, true);
        } else if (!subscription.getIsSubscribed()) {
            subscription.setIsSubscribed(true);
        } else {
            log.warn("Subscription {} already subscribed.", id);
            return;
        }

        subscriptionRepository.save(subscription);
        queueBinder.add(queueName);
        log.info("Added new subscription {}.", id);
    }

    @Synchronized
    protected void unsubscribe(UUID id) {
        Subscription subscription = subscriptionRepository.findByClientId(id);
        if (subscription == null) {
            log.warn("Subscription {} doesn't exist.", id);
            return;
        } else if (!subscription.getIsSubscribed()) {
            log.warn("Subscription {} already subscribed.", id);
            return;
        }

        subscription.setIsSubscribed(true);
        subscriptionRepository.save(subscription);
        queueBinder.remove(subscription.getQueueName());
        log.info("Removed subscription {}.", id);
    }
}
