package io.lastwill.eventscan.consumer;

import io.lastwill.eventscan.model.Subscription;
import io.lastwill.eventscan.repositories.SubscriptionRepository;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class SubscriptionConsumer {
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Synchronized
    public void subscribe(UUID id, String tcrAddress, String queueName) {
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
    }

    @Synchronized
    public void unsubscribe(UUID id) {
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
    }
}
