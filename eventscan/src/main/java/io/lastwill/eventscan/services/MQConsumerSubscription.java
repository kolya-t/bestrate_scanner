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

@Slf4j
@Component
public class MQConsumerSubscription {
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private QueueBinder queueBinder;

    @Synchronized
    @RabbitListener(queues = "${eventscan.mq.subscribe-queue.name}")
    public void subscribe(SubscribeMessage message) {
        Subscription subscription = subscriptionRepository.findByClientId(message.getId());
        if (subscription == null) {
            subscription = new Subscription(
                    message.getId(),
                    message.getAddress(),
                    message.getQueueName(),
                    true,
                    message.getBlockchain(),
                    message.getTokenAddress(),
                    message.getCurrency()
            );
        } else if (!subscription.getIsSubscribed()) {
            subscription.setIsSubscribed(true);
        } else {
            log.warn("Subscription {} already subscribed.", message.getId());
            return;
        }

        subscriptionRepository.save(subscription);
        queueBinder.add(message.getQueueName());
        log.info("Added new subscription {}.", message.getId());
    }

    @Synchronized
    @RabbitListener(queues = "${eventscan.mq.unsubscribe-queue.name}")
    public void unsubscribe(UnsubscribeMessage message) {
        Subscription subscription = subscriptionRepository.findByClientId(message.getId());
        if (subscription == null) {
            log.warn("Subscription {} doesn't exist.", message.getId());
            return;
        } else if (!subscription.getIsSubscribed()) {
            log.warn("Subscription {} already subscribed.", message.getId());
            return;
        }

        subscription.setIsSubscribed(false);
        subscriptionRepository.save(subscription);
        queueBinder.remove(subscription.getQueueName());
        log.info("Removed subscription {}.", message.getId());
    }
}
