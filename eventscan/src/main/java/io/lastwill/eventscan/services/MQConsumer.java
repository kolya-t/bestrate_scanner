package io.lastwill.eventscan.services;

import io.lastwill.eventscan.messages.in.SubscribeMessage;
import io.lastwill.eventscan.messages.in.UnsubscribeMessage;
import io.lastwill.eventscan.model.Subscription;
import io.lastwill.eventscan.repositories.SubscriptionRepository;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class MQConsumer {
//    public static final String SUBSCRIBE_QUEUE_NAME = "subscribeQueue";
//    public static final String UNSUBSCRIBE_QUEUE_NAME = "unsubscribeQueue";

    @Autowired
    private SubscriptionRepository subscriptionRepository;

//    @Autowired
//    @Qualifier(SUBSCRIBE_QUEUE_NAME)
//    private Queue subscribeQueue;
//
//    @Autowired
//    @Qualifier(UNSUBSCRIBE_QUEUE_NAME)
//    private Queue unsubscribeQueue;

    @RabbitListener(queues = "${io.lastwill.eventscan.mq.subscribe-queue.name}")
    public void subscribe(@Payload SubscribeMessage message) {
        subscribe(message.getId(), message.getTcrAddress(), message.getQueueName());
    }

    @RabbitListener(queues = "${io.lastwill.eventscan.mq.unsubscribe-queue.name}")
    public void unsubscribe(@Payload UnsubscribeMessage message) {
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
    }
}
