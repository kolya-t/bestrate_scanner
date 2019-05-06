package io.lastwill.eventscan.services.handlers;

import io.lastwill.eventscan.events.model.PaymentEvent;
import io.lastwill.eventscan.messages.out.PaymentMessage;
import io.lastwill.eventscan.services.MQProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnBean(MQProducer.class)
public class PaymentEventDispatcher {
    @Autowired
    private MQProducer producer;

    @EventListener
    private void onSubscriptionPayment(final PaymentEvent event) {
        try {
            producer.send(event.getSubscription().getQueueName(),
                    new PaymentMessage(
                            event.getNetworkType(),
                            event.getBlock().getHash(),
                            event.getTransaction().getHash(),
                            event.getAddressFrom(),
                            event.getAddressTo(),
                            event.getBlock().getTimestamp(),
                            event.getTokenAddress(),
                            event.getCurrency(),
                            event.getAmount(),
                            event.getFee(),
                            event.getMemo()
                    )
            );
        } catch (Throwable e) {
            log.error("Sending notification about payment failed.", e);
        }
    }
}
